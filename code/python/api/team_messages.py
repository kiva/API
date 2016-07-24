import requests as req 
import json
import time

url = "http://api.kivaws.org/v1/teams/search.json"
outfile = open("TEAM.json", "w")

# set range to desired pages
for i in range(1,550):
    params = dict(
        sort_by = 'oldest',
        page = i,

    )   
    resp = req.get(url = url, params = params)
    data = json.loads(resp.text)

    json.dump(data, outfile, sort_keys = True, indent = 2,
ensure_ascii=True)

    time.sleep(2)
