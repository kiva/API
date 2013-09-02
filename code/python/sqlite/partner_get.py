import httplib2
import sqlite3
from bs4 import BeautifulSoup
#estable http connection
http = httplib2.Http()
status, response = http.request('http://api.kivaws.org/v1/partners.html')
#use beautiful soup to parse 
soup = BeautifulSoup(response)
# if you have lxml installed then uncomment and use the next line
#soup = BeautifulSoup(response, "lxml")
lines=[]
# establish the connection to the sqlite db
conn = sqlite3.connect('kiva')
c = conn.cursor()
c.execute('CREATE TABLE IF NOT EXISTS partners (id tinyint primary key, name varchar(255), status varchar(10), rating varchar(20), due_dilegence_type varchar(12), start_date datetime, delinquency_rate float, default_rate float, total_amount_raised unsigned bigint, loans_posted smallint, portfolio_yield real)');
count = 0
# The [1:] syntax skips the first header row when we loop through all tr rows
for partner in soup.find_all('tr')[1:]:
	for td in partner:
		# i dont want all the data so purposefully limit the rows
		if(count < 11):
			text=td.renderContents().decode('utf-8')
			lines.append(text)
		count += 1
	c.execute('INSERT OR REPLACE INTO partners (id,name,status,rating,due_dilegence_type,start_date,delinquency_rate,default_rate,total_amount_raised,loans_posted,portfolio_yield) VALUES(?,?,?,?,?,?,?,?,?,?,?)', lines);
	lines=[]
	count = 0
conn.commit()
conn.close()
