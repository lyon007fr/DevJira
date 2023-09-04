
/***********************************************
*
*
*
*Script pour un listener
*
*Dans un listener l'issue est accessible directement via "issue" sans declaration prealable ou "event.issue"
*
*
************************************************/

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import com.atlassian.jira.event.type.EventDispatchOption
log.setLevel(org.apache.log4j.Level.DEBUG)

def issueManager =  ComponentAccessor.getIssueManager()
def customfieldManager = ComponentAccessor.getCustomFieldManager()
//def issueKey = ComponentAccessor.getIssueManager().getIssueByCurrentKey("GJA-9784")
def user = ComponentAccessor. getJiraAuthenticationContext().getLoggedInUser()

Issue issue = event.issue

//log.warn("id de l'event : " + event.getEventTypeId())
//log.warn("Statut actuel du ticket ${issue.key} : ${issue.status.getName()}")

//id de l'event generic qui se declenche apres chaque transition ==> 13
def idEventToCheck = 13L
def eventId = event.getEventTypeId()

String url = "https://monurl/"
//Issue issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("GJA-6366")
def serviceNowIdCf = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(10147L)
def CfValue = issue.getDescription()

def http = new HTTPBuilder(url)
String data = '{"u_project":"'+issue.key+'"}'
if(CfValue != null) {
    log.warn("Envoi de l'issue key a serviceNow")
    def response = http.request(Method.PATCH, ContentType.JSON) {
        uri.path = 'api/now/table/incident/' + CfValue
        headers.'Authorization' = "Basic ${"compte:motdepasse".bytes.encodeBase64().toString()}"
        headers.Accept = 'application/json'
        body = data
        response.failure = { failresp_inner ->
            failresp = failresp_inner.entity.content.text + " <br /> Code " + failresp_inner.status + " <br/> Headers" + failresp_inner.allHeaders
            log.debug failresp
            log.debug response
        }
    }
}


/***************************************************
*
* Envoi du statut a serviceNow dans les notes de travail
*
****************************************************/


//liste des status à checker, peut-être completer modifier directement ci-dessous
def listLabelStatusToCheck = ["Terminé","En cours"]
def issueStatus = issue.getStatus().getName()

if(listLabelStatusToCheck.contains(issueStatus) && eventId == idEventToCheck ){
    log.warn("Envoi du statut actuel a serviceNow")
    String url_comment = "https://monurl/"
    def http_comment = new HTTPBuilder(url_comment)
    String data_comment = '{"work_notes":"'+"Le ticket Jira (${issue.getKey()}) vient de passer au statut ==> "+issueStatus+'"}'

    def response = http_comment.request(Method.PUT, ContentType.JSON) {
        uri.path = 'api/now/table/incident/' + CfValue
        headers.'Authorization' = "Basic ${"passwordloginbase64"}"
        headers.Accept = 'application/json'
        body = data_comment
        response.failure = { failresp_inner ->
            failresp = failresp_inner2.entity.content.text + " <br /> Code " + failresp_inner.status + " <br/> Headers" + failresp_inner.allHeaders
            log.debug failresp
            log.debug response
        }
    }
    
}
   

/***************************************************
*
* Update du champ Lien serviceNow
*
****************************************************/


//mise a jour du champ permettant de visualiser l'url du ticket serviceNow
def serviceNowLinkCustomfield = customfieldManager.getCustomFieldObject(19000L)
def serviceNowLinkValue = serviceNowLinkCustomfield.getValue(issue)


if(!serviceNowLinkValue){
    
    def newValueForserviceNowLink = "https://servicenow"+CfValue
    log.warn("Update du champ Lien serviceNow avec la valeur : " + newValueForserviceNowLink)
    issue.setCustomFieldValue(serviceNowLinkCustomfield,newValueForserviceNowLink)
    def newIssue = issueManager.updateIssue(user,issue,EventDispatchOption.DO_NOT_DISPATCH,false)
    if(newIssue){
        log.warn("Lien serviceNow Update successful")
    }else {
        log.warn("Unsuccessful update of the Lien serviceNow")
    }
    
}
