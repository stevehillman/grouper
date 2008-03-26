/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberLite;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsAddMemberLiteResult;
import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 *
 * @author mchyzer
 *
 */
public class WsSampleAddMemberLite implements WsSampleGenerated {
    /**
     * @param wsSampleGeneratedType if SOAP or XML/HTTP
     */
    public static void addMemberLite(
        WsSampleGeneratedType wsSampleGeneratedType) {
        try {
            //URL, e.g. http://localhost:8091/grouper-ws/services/GrouperService
            GrouperServiceStub stub = new GrouperServiceStub(GeneratedClientSettings.URL);
            Options options = stub._getServiceClient().getOptions();
            HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
            auth.setUsername(GeneratedClientSettings.USER);
            auth.setPassword(GeneratedClientSettings.PASS);
            auth.setPreemptiveAuthentication(true);

            options.setProperty(HTTPConstants.AUTHENTICATE, auth);
            options.setProperty(HTTPConstants.SO_TIMEOUT, new Integer(3600000));
            options.setProperty(HTTPConstants.CONNECTION_TIMEOUT,
                new Integer(3600000));

            AddMemberLite addMemberLite = AddMemberLite.class.newInstance();

            //version, e.g. v1_3_000
            addMemberLite.setClientVersion(GeneratedClientSettings.VERSION);

            addMemberLite.setGroupName("aStem:aGroup");

            addMemberLite.setGroupUuid("");

            addMemberLite.setSubjectId("10021368");
            addMemberLite.setSubjectSourceId("");
            addMemberLite.setSubjectIdentifier("");

            // set the act as id
            addMemberLite.setActAsSubjectId("GrouperSystem");

            addMemberLite.setActAsSubjectSourceId("");
            addMemberLite.setActAsSubjectIdentifier("");
            addMemberLite.setFieldName("");
            addMemberLite.setIncludeGroupDetail("");
            addMemberLite.setIncludeSubjectDetail("");
            addMemberLite.setSubjectAttributeNames("");
            addMemberLite.setParamName0("");
            addMemberLite.setParamValue0("");
            addMemberLite.setParamName1("");
            addMemberLite.setParamValue1("");

            WsAddMemberLiteResult wsAddMemberLiteResult = stub.addMemberLite(addMemberLite)
                                                              .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsAddMemberLiteResult, ToStringStyle.MULTI_LINE_STYLE));
            System.out.println(ToStringBuilder.reflectionToString(
                    wsAddMemberLiteResult.getResultMetadata(),
                    ToStringStyle.MULTI_LINE_STYLE));
            System.out.println(ToStringBuilder.reflectionToString(
                    wsAddMemberLiteResult.getSubjectAttributeNames(),
                    ToStringStyle.MULTI_LINE_STYLE));
            System.out.println(ToStringBuilder.reflectionToString(
                    wsAddMemberLiteResult.getWsGroupAssigned(),
                    ToStringStyle.MULTI_LINE_STYLE));
            System.out.println(ToStringBuilder.reflectionToString(
                    wsAddMemberLiteResult.getWsSubject(),
                    ToStringStyle.MULTI_LINE_STYLE));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        addMemberLite(WsSampleGeneratedType.soap);
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        addMemberLite(wsSampleGeneratedType);
    }
}
