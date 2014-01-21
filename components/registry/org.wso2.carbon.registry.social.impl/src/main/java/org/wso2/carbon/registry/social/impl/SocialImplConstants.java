/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.registry.social.impl;

/**
 * The class having all constants related to the SocialAPI implementation
 */
public class SocialImplConstants {
    private SocialImplConstants() {

    }

    public static final String USER_REGISTRY_ROOT = "/users/";
    public static final String ACTIVITY_PATH = "/activities/";
    public static final String ACTIVITY_STREAM_PATH = "/activityEntries/";

    public static final String APP_DATA_REGISTRY_ROOT = "/appData"; // TODO: check path
    public static final String MESSAGES_PATH = "/messages";
    /* Relationship */
    public static final String ASS_TYPE_RELATIONSHIP = "FRIENDSHIP";
    public static final String PENDING_RELATIONSHIP_REQUEST_PATH = "/pendingRequests";
    public static final String RELATIONSHIP_REQUESTS_PROPERTY = "pendingRequests";
    public static final String RELATIONSHIP_STATUS_FRIEND = "friend";
    public static final String RELATIONSHIP_STATUS_REQUEST_PENDING = "requestpending";  // viewer's friend request pending
    public static final String RELATIONSHIP_STATUS_REQUEST_RECEIVED = "requestreceived"; // viewer has a request from this person
    public static final String RELATIONSHIP_STATUS_NONE = "none";
    public static final String RELATIONSHIP_STATUS_SELF = "self";

    /* Activities  */
    public static final String NEXT_ACTIVITY_ID_PATH = "/nextActivityId";
    public static final String NEXT_ACTIVITY_ID = "nextActivityId";

    /* Activity attributes */
    public static final String ACTIVITY_APP_ID = "appId";
    public static final String ACTIVITY_BODY_ID = "bodyId";
    public static final String ACTIVITY_BODY = "body";
    public static final String ACTIVITY_EXTERNAL_ID = "externalId";
    public static final String ACTIVITY_ID = "id";
    public static final String ACTIVITY_UPDATED = "updated";
    public static final String ACTIVITY_POSTED_TIME = "postedTime";
    public static final String ACTIVITY_PRIORITY = "priority";
    public static final String ACTIVITY_STREAM_FAVICON_URL = "streamFaviconUrl";
    public static final String ACTIVITY_STREAM_SOURCE_URL = "streamSourceUrl";
    public static final String ACTIVITY_STREAM_TITLE = "streamTitle";
    public static final String ACTIVITY_STREAM_URL = "streamUrl";
    public static final String ACTIVITY_TITLE = "title";
    public static final String ACTIVITY_TITLE_ID = "titleId";
    public static final String ACTIVITY_URL = "url";
    public static final String ACTIVITY_USER_ID = "userId";
    /* Activity MediaItem Fields */
    public static final String ACTIVITY_MEDIA_ITEM_PATH = "/mediaItem";
    public static final String ACTIVITY_MEDIA_ITEM_NOS = "noOfMediaItems";
    public static final String ACTIVITY_MEDIA_ITEM_MIME_TYPE = "mimeType";
    public static final String ACTIVITY_MEDIA_ITEM_TYPE = "type";
    public static final String ACTIVITY_MEDIA_ITEM_URL = "url";
    public static final String ACTIVITY_MEDIA_ITEM_THUMBNAIL_URL = "thumbnailUrl";


    public static final String ACTIVITY_TEMPLATE_PARAMS_PATH = "/templateParams";
    public static final int CHILD_RESOURCE_INDEX = 0;  //TODO? 0 or 1?

    /* Activity Stream Attributes */
    public static final String ACTIVITY_STREAM_ID = "id";
    public static final String ACTIVITY_STREAM_ACTOR = "actor";
    public static final String ACTIVITY_STREAM_CONTENT = "content";
    public static final String ACTIVITY_STREAM_GENERATOR = "generator";
    public static final String ACTIVITY_STREAM_OBJECT = "object";
    public static final String ACTIVITY_STREAM_PUBLISHED = "published";
    public static final String ACTIVITY_STREAM_PROVIDER = "provider";
    public static final String ACTIVITY_STREAM_TARGET = "target";
    public static final String ACTIVITYSTREAM_TITLE = "title";
    public static final String ACTIVITY_STREAM_UPDATED = "updated";
    public static final String ACTIVITYSTREAM_URL = "url";
    public static final String ACTIVITY_STREAM_VERB = "verb";
    public static final String ACTIVITY_STREAM_ICON = "icon";
    public static final String ACTIVITY_STREAM_ACTOR_PATH = "/actor";
    public static final String ACTIVITY_STREAM_ACTOR_MEDIA_PATH = "/actorMediaLink";
    public static final String ACTIVITY_STREAM_GENERATOR_PATH = "/generator";
    public static final String ACTIVITY_STREAM_GENERATOR_MEDIA_PATH = "/generatorMediaLink";
    public static final String ACTIVITY_STREAM_TARGET_PATH = "/target";
    public static final String ACTIVITY_STREAM_TARGET_MEDIA_PATH = "/targetMediaLink";
    public static final String ACTIVITY_STREAM_PROVIDER_PATH = "/provider";
    public static final String ACTIVITY_STREAM_PROVIDER_MEDIA_PATH = "/providerMediaLink";

    /* Activity Stream Objects attributes */
    public static final String ACTIVITY_STREAM_OBJECT_PATH = "/object";
    public static final String ACTIVITY_STREAM_OBJECT_MEDIA_PATH = "/objectMediaLink";
    public static final String ACTIVITY_STREAM_OBJECT_ID = "id";
    public static final String ACTIVITY_STREAM_OBJECT_SUMMARY = "summary";
    public static final String ACTIVITY_STREAM_OBJECT_IMAGE = "image";
    public static final String ACTIVITY_STREAM_OBJECT_ATTACHMENTS = "attachments";
    public static final String ACTIVITY_STREAM_OBJECT_AUTHOR = "author";
    public static final String ACTIVITY_STREAM_OBJECT_CONTENT = "content";
    public static final String ACTIVITY_STREAM_OBJECT_DISPLAYNAME = "displayName";
    public static final String ACTIVITY_STREAM_OBJECT_DOWNSTREAM_DUPLICATES = "downstreamDuplicates";
    public static final String ACTIVITY_STREAM_OBJECT_UPSTREAM_DUPLICATES = "upstreamDuplicates";
    public static final String ACTIVITYSTREAM_OBJECT_TYPE = "objectType";
    public static final String ACTIVITY_STREAM_OBJECT_UPDATED = "updated";
    public static final String ACTIVITYSTREAM_OBJECT_PUBLISHED = "published";
    public static final String ACTIVITY_STREAM_OBJECT_URL = "url";


    /* ActivityStream MediaLink Fields */
    public static final String ACTIVITY_STREAM_MEDIA_PATH = "/mediaLink";
    public static final String ACTIVITY_STREAM_MEDIA_DURATION = "duration";
    public static final String ACTIVITY_STREAM_MEDIA_WIDTH = "width";
    public static final String ACTIVITY_STREAM_MEDIA_HEIGHT = "height";
    public static final String ACTIVITY_STREAM_MEDIA_URL = "url";

    /* MessageCollection Fields */
    public static final String MSG_COLLECTION_ID = "id";
    public static final String MSG_COLLECTION_TITLE = "title";
    public static final String MSG_COLLECTION_TOTAL_MESSAGES = "total";
    public static final String MSG_COLLECTION_UNREAD_MESSAGES = "unread";
    public static final String MSG_COLLECTION_UPDATED_DATE = "updated";
    public static final String MSG_COLLECTION_URLS = "urls";


    /* Message Fields */
    public static final String MSG_APP_URL = "appUrl";
    public static final String MSG_BODY = "body";
    public static final String MSG_BODY_ID = "bodyId";
    public static final String MSG_COLLECTION_IDS = "collectionIds";
    public static final String MSG_ID = "id";
    public static final String MSG_IN_REPLY_TO = "inReplyTo";
    public static final String MSG_RECIPIENTS = "recipients";
    public static final String MSG_REPLIES = "replies";
    public static final String MSG_SENDER_ID = "senderId";
    public static final String MSG_STATUS = "status";
    public static final String MSG_TIME_SENT = "timeSent";
    public static final String MSG_TITLE = "title";
    public static final String MSG_TITLE_ID = "titleId";
    public static final String MSG_TYPE = "type";
    public static final String MSG_UPDATED = "updated";
    public static final String MSG_URLS = "urls";


    /* Claim  Urls*/
    public static final String CLAIM_URI_DISPLAY_NAME = "http://wso2.org/claims/displayname";
    public static final String CLAIM_URI_GIVEN_NAME = "http://wso2.org/claims/givenname";
    public static final String CLAIM_URI_FAMILY_NAME = "http://wso2.org/claims/lastname";
    public static final String CLAIM_URI_NICK_NAME = "http://wso2.org/claims/nickname";
    public static final String CLAIM_URI_ORGANIZATION = "http://wso2.org/claims/organization";
    public static final String CLAIM_URI_STREET_ADDRESS = "http://wso2.org/claims/streetaddress";
    public static final String CLAIM_URI_REGION = "http://wso2.org/claims/region";
    public static final String CLAIM_URI_COUNTRY = "http://wso2.org/claims/country";
    public static final String CLAIM_URI_LATITUDE = "http://wso2.org/claims/latitude";
    public static final String CLAIM_URI_LONGITUDE = "http://wso2.org/claims/longitude";
    public static final String CLAIM_URI_POSTAL_CODE = "http://wso2.org/claims/postalcode";
    public static final String CLAIM_URI_EMAIL = "http://wso2.org/claims/emailaddress";
    public static final String CLAIM_URI_PHONE_NUMBER = "http://wso2.org/claims/telephone";
    public static final String CLAIM_URI_IM = "http://wso2.org/claims/im";
    public static final String CLAIM_URI_URL = "http://wso2.org/claims/url";
    public static final String CLAIM_URI_ABOUT_ME = "http://wso2.org/claims/aboutme";
    public static final String CLAIM_URI_BIRTHDAY = "http://wso2.org/claims/birthday";
    public static final String CLAIM_URI_RELATIONSHIP_STATUS = "http://wso2.org/claims/relationshipstatus";
    public static final String CLAIM_URI_RELIGIOUS_VIEW = "http://wso2.org/claims/religion";
    public static final String CLAIM_URI_ETHNICITY = "http://wso2.org/claims/ethnicity";
    public static final String CLAIM_URI_GENDER = "http://wso2.org/claims/gender";
    public static final String CLAIM_URI_POLITICAL_VIEW = "http://wso2.org/claims/politicalviews";
    public static final String CLAIM_URI_INTERESTS = "http://wso2.org/claims/interests";
    public static final String CLAIM_URI_BOOKS = "http://wso2.org/claims/books";
    public static final String CLAIM_URI_JOB_INTERESTS = "http://wso2.org/claims/jobInterests";
    public static final String CLAIM_URI_LANGUAGE_SPOKEN = "http://wso2.org/claims/languagespoken";
    public static final String CLAIM_URI_LOOKING_FOR = "http://wso2.org/claims/lookingfor";
    public static final String CLAIM_URI_MOVIES = "http://wso2.org/claims/movies";
    public static final String CLAIM_URI_MUSIC = "http://wso2.org/claims/music";
    public static final String CLAIM_URI_QUOTES = "http://wso2.org/claims/quotes";
    public static final String CLAIM_URI_HAPPIEST_WHEN = "http://wso2.org/claims/happiestwhen";


    /* Person  Fields */
    public static final String FIELD_ABOUT_ME = "ABOUT_ME";
    public static final String FIELD_DISPLAY_NAME = "DISPLAY_NAME";
    public static final String FIELD_NAME = "NAME";
    public static final String FIELD_BOOKS = "BOOKS";
    public static final String FIELD_ADDRESSES = "ADDRESSES";
    public static final String FIELD_NICKNAME = "NICKNAME";
    public static final String FIELD_BIRTHDAY = "BIRTHDAY";
    public static final String FIELD_EMAILS = "EMAILS";
    public static final String FIELD_ETHNICITY = "ETHNICITY";
    public static final String FIELD_HAPPIEST_WHEN = "HAPPIEST_WHEN";
    public static final String FIELD_IM = "IM";
    public static final String FIELD_INTERESTS = "INTERESTS";
    public static final String FIELD_JOB_INTERESTS = "JOB_INTERESTS";
    public static final String FIELD_LANGUAGE_SPOKEN = "LANGUAGE_SPOKEN";
    public static final String FIELD_LOOKING_FOR = "LOOKING_FOR";
    public static final String FIELD_MUSIC = "MUSIC";
    public static final String FIELD_MOVIES = "MOVIES";
    public static final String FIELD_QUOTES = "QUOTES";
    public static final String FIELD_POLITICAL_VIEW = "POLITICAL_VIEW";
    public static final String FIELD_GENDER = "GENDER";
    public static final String FIELD_RELATIONSHIP_STATUS = "RELATIONSHIP_STATUS";
    public static final String FIELD_RELIGION = "RELIGION";
    public static final String FIELD_PHONE_NUMBERS = "PHONE_NUMBERS";


    /* Group  ID */
    public static final String GROUP_ID_SELF = "self";
    public static final String GROUP_ID_FRIENDS = "friends";
    public static final String GROUP_ID_ALL = "all";
    public static final String GROUP_ID_TOP_FRIENDS = "topfriends"; //TODO: implementation


    public static final String SEPARATOR = "/";


    public static final int DEFAULT_RETURN_ARRAY_SIZE = 20;
    public static final String DEFAULT_USER_FILTER_STRING = "*";


}
