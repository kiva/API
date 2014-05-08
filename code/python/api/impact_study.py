import requests

class TestKivaAPI():

    # Return a list of teams a lender belongs to
    def get_team_list(self,lender_id):
        url = 'https://api.kivaws.org/v1/lenders/'+lender_id+'/teams.json'
        response = requests.get(url)
        if response.status_code != requests.codes.ok:  self.handle_error(response.status_code)

        data = response.json()
        team_list = data['teams']
        return team_list

    # Return the list of lenders in a team
    def get_lender_in_team_list(self, team_list, team_id):
        url = 'https://api.kivaws.org/v1/teams/'+str(team_list[team_id]['id'])+'/lenders.json'
        response = requests.get(url)
        if response.status_code != requests.codes.ok:  self.handle_error(response.status_code)

        data = response.json()

        # Get the list of lenders on this team
        lender_list = data['lenders']
        #print 'lender_list has '+ str(len(lender_list))+ ' entries'
        return lender_list

    # Return a list of comma-separated ids from the list of lenders
    def build_lender_id_list(self, lender_list):
        every_id = ''
        for j in range(len(lender_list)):
        # When lenders choose to be anonymous, we do not check their invitation count
            if ('name' in lender_list[j].keys()):
                if (lender_list[j]['name'] != 'Anonymous'):
                    id = lender_list[j]['lender_id']
                    every_id += id +','
        every_id = every_id[:-1] #remove the final comma
        return every_id

    # Return the total number of invites extended by the list of lenders
    def get_lender_invitee_sum(self, id_list):
        # Make a single GET request to retrieve a list of lender details
        url = 'https://api.kivaws.org/v1/lenders/'+id_list+'.json'
        response = requests.get(url)
        if response.status_code != requests.codes.ok:  self.handle_error()
        data = response.json()

        # Go through each lender in the list and add up their invitations for a team total
        lender_list = data['lenders']
        team_impact = 0
        for j in range(len(lender_list)):
            team_impact += lender_list[j]['invitee_count']
        return team_impact

    def handle_error(self, status):
        if status == 'org.kiva.RateLimitExceeded':
            print('Status: ', response.status_code, 'You are blocked due to overuse. Retry in a few minutes. Exiting.')
        elif status == 400:
            print('Your url was wrong:', status, '. Exiting.')
        elif status == 403:
            print('Status: ', status, 'You are forbidden from requesting this resource at this moment. Retry in a few minutes. Exiting. ')
        else:
            print('Status: ', status, 'Problem with the request. Exiting')
        exit()

def main():
    #Change 'premal' to your own lender id to see the impact of the teams you belong to
    lender_id = 'premal'
    test = TestKivaAPI()
    team_list = test.get_team_list(lender_id)
    for i in range(len(team_list)):
        print 'Team id is %d' % team_list[i]['id']
        lender_list = test.get_lender_in_team_list(team_list, i)
        id_list = test.build_lender_id_list(lender_list)
        team_impact = test.get_lender_invitee_sum(id_list)
        print "Team %10s invited %d others" % (team_list[i]['name'], team_impact )

if __name__ == '__main__':
    main()

