import json
import csv

f = open('BuildingList_json.txt')
data = json.load(f)
f.close()

f = open('buildings.csv', 'w')
csv_file=csv.writer(f)
for item in data['BuildingList']:
    csv_file.writerow([item["Title"], item["Alias"] ,item["Latitude"], item["Longitude"]])
f.close()
