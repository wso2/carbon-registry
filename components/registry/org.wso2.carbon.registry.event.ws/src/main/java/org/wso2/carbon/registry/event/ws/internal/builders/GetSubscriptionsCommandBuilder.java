/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.event.ws.internal.builders;

import java.util.Calendar;
import java.util.List;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.wso2.carbon.registry.event.core.subscription.Subscription;
import org.wso2.carbon.registry.event.ws.internal.util.EventingConstants;

public class GetSubscriptionsCommandBuilder {
    private static SOAPFactory fac = OMAbstractFactory.getSOAP12Factory();

    public static SOAPEnvelope buildResponseforGetSubscriptions(List<Subscription> subscriptions, int maxResultCount, int startIndex){
        OMNamespace wseexns = fac.createOMNamespace(EventingConstants.WSE_EXTENDED_EVENTING_NS,"wseex");
        
        OMElement response = fac.createOMElement("getSubscriptionsResponse",wseexns);
        
        int addedCount = 0;
        
        for(int i = startIndex; i< Math.min(subscriptions.size(), startIndex+ maxResultCount); i++){
            Subscription subscription = subscriptions.get(i);
            OMElement subsciptionEle = fac.createOMElement("subscriptionDetail", wseexns, response);
            //fac.createOMElement("dialect", wseexns,subsciptionEle).setText("");
            fac.createOMElement("subscriptionId", wseexns,subsciptionEle).setText(subscription.getId());
            fac.createOMElement("eventSinkAddress", wseexns,subsciptionEle).setText(subscription.getEventSinkURL());
            if(subscription.getExpires() != null){
                //Time time = new Ti
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(subscription.getExpires().getTimeInMillis());
                fac.createOMElement("subscriptionEndingTime", wseexns,subsciptionEle).setText(ConverterUtil.convertToString(calendar));    
            }
            fac.createOMElement("topic", wseexns,subsciptionEle).setText(subscription.getTopicName());
            if(subscription.getCreatedTime() != null){
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(subscription.getCreatedTime().getTime());
                fac.createOMElement("createdTime", wseexns,subsciptionEle)
                    .setText(ConverterUtil.convertToString(calendar));
            }
            addedCount++;
        }
        
        response.addAttribute("allRequestCount", String.valueOf(subscriptions.size()), null);
        if(startIndex+ maxResultCount < subscriptions.size()){
            response.addAttribute("hasMoreResults", "true", null);
        }
        
        SOAPEnvelope envelope = fac.getDefaultEnvelope();
        envelope.getBody().addChild(response);
        return envelope;
    }
}
