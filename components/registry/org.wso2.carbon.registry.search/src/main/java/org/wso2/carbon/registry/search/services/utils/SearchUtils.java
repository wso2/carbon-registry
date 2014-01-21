package org.wso2.carbon.registry.search.services.utils;

import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.search.beans.AdvancedSearchResultsBean;

import java.util.regex.Pattern;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SearchUtils {

//    These invalid characters are not as same as the ones in registry core. There is a slight modification
    private static final String ILLEGAL_CHARACTERS_FOR_PATH = ".*[~!@#;^*+={}\\|\\\\<>\",\'].*";
    private static final String ILLEGAL_CHARACTERS_FOR_DATE = "^(0[1-9]|1[012])[- /.](0[1-9]|[12][0-9]|3[01])[- /.](19|20)\\d\\d$";
    private static final String ILLEGAL_CHARACTERS_FOR_MEDIA_TYPE = ".*[~!@#;^*={}\\|\\\\<>\",\'].*";
    private static final String ILLEGAL_CHARACTERS_FOR_CONTENT = ".*[~!@#;%^*+{}\\|\\\\<>\\\"\\',\\[\\]\\(\\)].*";
    private static final String ILLEGAL_CHARACTERS_FOR_TAGS = ".*[~!@#;%^*+={}\\|\\\\<>\"'].*";

    private static Pattern illegalCharactersForPathPattern = Pattern.compile(ILLEGAL_CHARACTERS_FOR_PATH);
    private static Pattern illegalCharactersForDatePattern = Pattern.compile(ILLEGAL_CHARACTERS_FOR_DATE);
    private static Pattern illegalCharactersForMediaTypePattern = Pattern.compile(ILLEGAL_CHARACTERS_FOR_MEDIA_TYPE);
    private static Pattern illegalCharactersForContentPattern  = Pattern.compile(ILLEGAL_CHARACTERS_FOR_CONTENT);
    private static Pattern illegalCharactersForTagsPattern  = Pattern.compile(ILLEGAL_CHARACTERS_FOR_TAGS);


    public static boolean validatePathInput(String input){
        return input != null && illegalCharactersForPathPattern.matcher(input).matches();
    }
    public static boolean validateDateInput(String input){
        return input != null && illegalCharactersForDatePattern.matcher(input).matches();
    }
    public static boolean validateMediaTypeInput(String input){
        return input != null && illegalCharactersForMediaTypePattern.matcher(input).matches();
    }
    public static boolean validateContentInput(String input){
        return input != null && illegalCharactersForContentPattern.matcher(input).matches();
    }
    public static boolean validateTagsInput(String input){
        return input != null && illegalCharactersForTagsPattern.matcher(input).matches();
    }

    public static void sortResourceDataList(List<ResourceData> resourceDataList) {
        Collections.sort(resourceDataList, new Comparator<ResourceData>() {
            public int compare(ResourceData o1, ResourceData o2) {
                return o1.getResourcePath().toLowerCase().compareTo(
                        o2.getResourcePath().toLowerCase());
            }
        });
    }

    public static AdvancedSearchResultsBean getEmptyResultBeanWithErrorMsg(String msg){

        AdvancedSearchResultsBean metaDataSearchResultsBean = new AdvancedSearchResultsBean();
        metaDataSearchResultsBean.setErrorMessage(msg);
        return metaDataSearchResultsBean;
    }
}
