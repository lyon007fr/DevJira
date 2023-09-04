"""

Script permettant de faire du scrapping afin de récuperer les group et utilisateur admin d'un espace Confluence (a ce jour pas d'api rest pour le faire)

"""

import requests
import urllib3
urllib3.disable_warnings() #permet d'enlever les warning liés aux appeles API (sera corrigé en ajoutant le certificat dans l'appel api à la place de "vérify = false")
from bs4 import BeautifulSoup

URL = "https://confluence.fr/spaces/spacepermissions.action?key=KeyToWatch"
page = requests.get(URL,auth=('username','password'),verify=False)

#print(page.text)
soup = BeautifulSoup(page.content, "html.parser")

#results = soup.find(id="gPermissionsTable")
results = soup.find_all("table", class_="permissions aui")
#print(results[1])

groupWithAdminPermission = []
userWithAdminPermission = []

for i in results[0].find_all("tr",class_="space-permission-row"):
    data = i.find_all("td", class_="permissionCell")
    lastElement = data[-1]
    image = lastElement.find('img').get('src')
    if image.rsplit('/', 1)[-1] == "check.png":
        groupWithAdminPermission.append(i.find("td").text.strip())
    

for i in results[1].find_all("tr",class_="key-holder"):
    data = i.find_all("td", class_="permissionCell")
    lastElement = data[-1]
    image = lastElement.find('img').get('src')
    if image.rsplit('/', 1)[-1] == "check.png":
        userWithAdminPermission.append(' '.join(i.find("td").text.split()[:-1]))


print('Les groupes admin sont : \n' + "\n".join(groupWithAdminPermission) + '\n') 
print('Les utilisateur admin sont : \n' + "\n".join(userWithAdminPermission)) 
