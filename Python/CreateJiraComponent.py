"""

Code permettant de créer des composant depuis un fichier excel, en utilisant pandas pour lire le fichier excel

"""

import requests
import pandas as pd
import urllib3
urllib3.disable_warnings() #permet d'enlever les warning liés aux appeles API (sera corrigé en ajoutant le certificat dans l'appel api à la place de "vérify = false")

#https://confluence.atlassian.com/enterprise/using-personal-access-tokens-1026032365.html
token = ""
base_url = "https://jira.fr/"

proxies = {'https':'http://proxy.fr:331'}

#lecture du fichier
file = pd.read_excel('C:\\Users\\Downloads\\Component.xlsx')  


#https://pandas.pydata.org/docs/reference/api/pandas.DataFrame.values.html
for i in file.values:
    payload = {
    "name": i[0].strip(),#suppression des espace car le fichier excel a des espaces dans cette colonne
    "leadUserName": i[1].strip(),#suppression des espace car le fichier excel a des espaces dans cette colonne
    "assigneeType": "COMPONENT_LEAD",
    "isAssigneeTypeValid": False,
    "project": "AAA",
    "projectId": 29500
}

    #utilisation du token Jira
    r = requests.post(base_url+'rest/api/2/component',json=payload,headers={'Authorization': token}, verify=False)
    try:
        r.raise_for_status()
    except requests.exceptions.RequestException:
        print(f'Erreur {r.status_code} pour le composant {i[0]}')
