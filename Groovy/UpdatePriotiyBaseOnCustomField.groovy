/*

Update du champ priorité en fonction de la valeur d'un champ présent sur le même ticket.

*/

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.fields.config.manager.PrioritySchemeManager
import com.atlassian.jira.event.type.EventDispatchOption

def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("ABC-10380")
def issueManager = ComponentAccessor.getIssueManager()
def astridPriority = ComponentAccessor.getCustomFieldManager().getCustomFieldObjects(issue).find { cf -> cf.name == 'Priorité de 0 (+) à 100 (-)' }
def gojiraAdminUser = ComponentAccessor.getUserManager().getUserByName("jira-admin")

def astridPriorityValue = astridPriority.getValue(issue) as int

def project = issue.getProjectObject()
def priorityScheme = ComponentAccessor.getComponent(PrioritySchemeManager)
def schemePriorityName = priorityScheme.getScheme(project).getName()
log.warn(schemePriorityName)

//dictionnaire pour mapper les labels des priority scheme avec un ID
def prioritiesMap = ["Default" : 1,"MajMin" : 2, "Moscow": 3, "OptionNonPriorisé" : 4 ,"TargetProcess" : 5, 
"DefaultBloquante" : 6 , "MajMin" : 7 , "MajMin" : 8]
//log.warn(prioritiesMap[schemePriorityName])

//liste de liste contenant la priorité Astrid en index 0 (un range) et les labels des priorités Jira
def prioritiesList = [[1..19,"Haute","Majeure","Must","Haute","Majeure","Haute","Majeure","Majeure"],
[20..39,"Moyenne","Moyenne","Should","Moyenne","Majeure","Moyenne","Moyenne","Majeure"],
[40..57,"Basse","Moyenne","Should","Basse","Mineure","Basse","Moyenne","Majeure"],
[58..100,"Très Basse","Mineure","Could","Très Basse","Mineure","Très Basse","Mineure","Mineure"]]

for(i in prioritiesList){
    if(i[0].containsWithinBounds(astridPriorityValue)){
        def priotiryToSet = i[prioritiesMap[schemePriorityName]]
        log.warn(priotiryToSet)
        def priorityToSetId = ComponentAccessor.getConstantsManager().getPriorityObjects().find {it.name == priotiryToSet}?.getId()
        log.warn(priorityToSetId)
        if (! priorityToSetId){
            log.warn("Impossible de trouver un priorité avec le nom : $priotiryToSet")

        }else{
            issue.setPriorityId(priorityToSetId)
            issueManager.updateIssue(gojiraAdminUser, issue, EventDispatchOption.DO_NOT_DISPATCH, false)
        }
            
    }
    }