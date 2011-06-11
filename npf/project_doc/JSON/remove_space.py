f = open("buildings.csv","r")
g = open("buildings_nospace.csv", "w")

i = 0

for line in f:
	if i%2==0:
		print line
		i+=1

print i
