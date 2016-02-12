fb_graph is commandline scala app for automating mundane tasks involving Facebook Graph API.

### Requirement
1. Scala
2. SBT

### Currently Supported
1. Automate (almost) creating test users. 

2. Automate deleting all test users.

### Usage

#### Create Test Users
1. Write `config.json` in root directory with the format:

```
{
  "app_id": "{FB_APP_ID}",
  "app_secret": "{FB_APP_SECRET}",
  "test_users": [
    {
      "name": "Bill Gates",
      "locale": "en_US",
      "profilePictureURL": "http://aib.edu.au/blog/wp-content/uploads/2015/08/bill-gates-jpg.jpg",
      "coverPhotoURL": "http://www.teengazette.com/wp-content/uploads/2016/01/o-BILL-GATES-facebook.jpg"
    },
    ....
  ]
}
```

2. Run `sbt "run test-user create"`

3. Wait for app to print out:
```
Test User
Access Token: 
Email: 
Password: 
CoverPhotoPreviewURL: 
```

4. Login to facebook with provided `email` and `password` and simply make uploaded photos profile picture or cover photo.   
This part cannot be automated since facebook prohibits updating profile picture / cover photo through API.


#### Delete All Test Users
1. Write `config.json` as above (`test_users` is not needed).

2. Run `sbt "run test-user deleteAll"`

3. Done