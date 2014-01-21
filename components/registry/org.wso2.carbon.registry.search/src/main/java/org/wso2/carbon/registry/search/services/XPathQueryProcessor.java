/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.search.services;

import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.dataaccess.QueryProcessor;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.utils.MediaTypesUtils;

import java.util.*;

public class XPathQueryProcessor implements QueryProcessor {

    public static final String XPATH_QUERY_MEDIA_TYPE = "application/vnd.wso2.xpath.query";

    private static final String NO_CONTAINMENT_SUFFIX = "(.)";

    private MetadataSearchService service;

    public XPathQueryProcessor(MetadataSearchService service) {
        this.service = service;
    }

    public Collection executeQuery(Registry registry, Resource resource, Map map)
            throws RegistryException {
        if (map != null &&
                map.containsKey("query") && XPATH_QUERY_MEDIA_TYPE.equals(map.get("mediaType"))) {
            String query = ((String) map.get("query")).substring(1);
            String[] parts;
            Set<String> paths;
            int partsRead = 0;
            if (query.indexOf('/') == 0) {
                // this is a search anywhere query.
                query = query.substring(1);
                parts = query.split("/");
                paths = executeQueryForPart(parts[0]);
                if (paths.size() == 0) {
                    // if there we no hits, then no point reading more.
                    partsRead = Integer.MAX_VALUE;
                } else {
                    // if there were some hits, then move to the next part.
                    partsRead = 1;
                }
            } else {
                // this is a search from root query.
                paths = Collections.singleton("/");
                parts = query.split("/");
            }
            if (parts.length > partsRead) {
                if (partsRead == 1) {
                    parts = query.substring(query.indexOf("/") + 1).split("/");
                }
                for (int i = 0; i < parts.length; i++) {
                    String part = parts[i];
                    String relation;
                    Set<String> matches;
                    if (part.contains("[")) {
                        matches = executeQueryForPart("%" + part.substring(
                                part.indexOf("[")));
                        relation = part.substring(0, part.indexOf("["));
                    } else {
                        relation = part;
                        matches = null;
                    }
                    Set<String> associations = new HashSet<String>();
                    Set<String> temp = new HashSet<String>();
                    for (String path : paths) {
                        Association[] list;
                        if (relation.endsWith(NO_CONTAINMENT_SUFFIX)) {
                            list = registry.getAssociations(path, relation.substring(0,
                                    relation.length() - NO_CONTAINMENT_SUFFIX.length()));
                        } else {
                            list = registry.getAssociations(path, relation);
                            String resourcePath = path + RegistryConstants.PATH_SEPARATOR +
                                    relation;
                            if (registry.resourceExists(resourcePath)) {
                                if (i == parts.length - 1) {
                                    // if this is the last part, then add children.
                                    Resource containedResource = registry.get(resourcePath);
                                    if (containedResource instanceof Collection) {
                                        String[] children =
                                                ((Collection) containedResource).getChildren();
                                        if (children != null) {
                                            temp.addAll(Arrays.asList(children));
                                        }
                                    } else {
                                        temp.add(resourcePath);
                                    }
                                } else {
                                    temp.add(resourcePath);
                                }
                            }
                        }
                        for (Association association : list) {
                            if (association.getSourcePath().equals(path) &&
                                    association.getDestinationPath().startsWith(
                                            RegistryConstants.ROOT_PATH)) {
                                associations.add(association.getDestinationPath());
                            }
                        }
                    }

                    temp.addAll(associations);
                    if (matches != null) {
                        // if matching did not happen, the matches would be null. Matches would be
                        // empty if matching happened and no matches were found. This is a different
                        // scenario.
                        temp.retainAll(matches);
                    }
                    paths = temp;
                }
            }

            return new CollectionImpl(paths.toArray(new String[paths.size()]));
        } else if (resource != null && XPATH_QUERY_MEDIA_TYPE.equals(resource.getMediaType())) {
            throw new UnsupportedOperationException("Query-resource model is not supported");
        } else {
            throw new RegistryException("Unable to process XPath query. Pre-conditions have not " +
                    "been set due to some unknown reason.");
        }
    }

    protected Set<String> executeQueryForPart(String part) throws RegistryException {
        Set<String> paths = new HashSet<String>();
        String[] subParts = part.split("\\[");
        String mediaType = MediaTypesUtils.getMimeTypeFromHumanReadableMediaType(subParts[0]);
        Map<String, String> base;
        if (mediaType == null || mediaType.equals(subParts[0])) {
            base = Collections.singletonMap("resourcePath", subParts[0]);
        } else {
            base = Collections.singletonMap("mediaType", mediaType);
        }
        if (subParts.length > 1) {
            String predicates = subParts[1].substring(0, subParts[1].length() - 1).trim();
            String[] predicateParts = predicates.split(" or ");
            for (String predicatePart : predicateParts) {
                Map<String, String> input = parsePredicatePart(base, predicatePart);
                ResourceData[] results = service.search(CurrentSession.getTenantId(), input);
                for (ResourceData result : results) {
                    paths.add(result.getResourcePath());
                }
            }
        } else {
            ResourceData[] results = service.search(CurrentSession.getTenantId(), base);
            for (ResourceData result : results) {
                paths.add(result.getResourcePath());
            }
        }
        return paths;
    }

    protected Map<String, String> parsePredicatePart(Map<String, String> base, String predicatePart) {
        Map<String, String> input = new HashMap<String, String>(base);
        String[] expressions = predicatePart.trim().split(" and ");
        for (String expression : expressions) {
            String expr = expression.trim();
            if (expr.contains("!=")) {
                String[] temp = fixParams(expr.split("!="));
                addNegatedItemsToInput(input, temp);
            } else if (expr.contains(">=")) {
                String[] temp = fixParams(expr.split(">="));
                input.put("propertyName", temp[0]);
                input.put("rightPropertyValue", temp[1]);
                input.put("rightOp", "ge");
            } else if (expr.contains(">")) {
                String[] temp = fixParams(expr.split(">"));
                input.put("propertyName", temp[0]);
                input.put("rightPropertyValue", temp[1]);
                input.put("rightOp", "gt");
            } else if (expr.contains("<=")) {
                String[] temp = fixParams(expr.split("<="));
                input.put("propertyName", temp[0]);
                input.put("leftPropertyValue", temp[1]);
                input.put("leftOp", "le");
            } else if (expr.contains("=")) {
                String[] temp = fixParams(expr.split("="));
                input.put(temp[0], temp[1]);
            }
        }
        return input;
    }

    protected void addNegatedItemsToInput(Map<String, String> input, String[] temp) {
        if (temp[0].equals("author")) {
            input.put("authorNameNegate", Boolean.toString(true));
        } else if (temp[0].equals("updater")) {
            input.put("updaterNameNegate", Boolean.toString(true));
        } else if (temp[0].equals("mediaType")) {
            input.put("mediaTypeNegate", Boolean.toString(true));
        } else if (temp[0].startsWith("created")) {
            input.put("createdRangeNegate", Boolean.toString(true));
        } else if (temp[0].startsWith("updated")) {
            input.put("updatedRangeNegate", Boolean.toString(true));
        } else {
            // we cannot do a '!=' comparison for other situations.
            return;
        }
        input.put(temp[0], temp[1]);
    }

    protected String[] fixParams(String[] temp) {
        String[] strings = Arrays.copyOf(temp, temp.length);
        strings[0] = strings[0].trim();
        if (strings[0].startsWith("@")) {
            strings[0] = strings[0].substring(1);
        }
        strings[1] = strings[1].trim();
        if (strings[1].indexOf('\'') == 0) {
            strings[1] = strings[1].substring(1, strings[1].length() - 1);
        }
        return strings;
    }
}
