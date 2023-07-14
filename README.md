# Simple File Transfer Android

<a href='https://play.google.com/store/apps/details?id=software.isratech.filetransferos&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1' target='_blank'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' style='width: 180px'/></a>

This app can be used to transfer files easily between two hosts on any operating system. 

*This app is specifally designed for Android, see below for other operating systems.*

It is the companion app to the python script [ptransfer](https://github.com/leolion3/Portfolio/tree/master/Python/FileSender)

Licensed in accords with the Apache License 2.0

## Author

Created and authored by Leonard Haddad.

<a href="https://leolion3.github.io/Portfolio" target="_blank"><img src="https://raw.githubusercontent.com/danielcranney/readme-generator/main/public/icons/socials/github.svg" width="16" height="16"> Portfolio</a>

## Demo

<div class="container">
	<img src="https://play-lh.googleusercontent.com/iHuddteA7OHsEr9jMnzo15gXJbZaAOHeEBDGZ_cv1u1MF6mOJGNhSMnETmQMnQlc1nI=w526-h296-rw" width="17%">
	<img src="https://play-lh.googleusercontent.com/SG7YI3_F70NN1pWVLh4AJXPycm2YC9XSNjCwigQB6N7Q64leSXaZXaDoPxC4uTPebu5z=w526-h296-rw" width="17%">
	<img src="https://play-lh.googleusercontent.com/AR9QsUbVkMCtYRkU_MVl8jDhLWcjWvQIWtftM-4l1OxbuZ9LmGpsh6k3m2B4uMCK_ZBW=w526-h296-rw" width="17%">
	<img src="https://play-lh.googleusercontent.com/9mJW796gIMOoZ3HULnPw9pOxOhhjfhPLp9veOMIw-FBtW1ZVWTByD3t49aLNbCHOyVhI=w526-h296-rw" width="17%">
</div>

## Communication

The following illustrates the communication occurring between both clients before, during and after the file transfer.

The illustration uses the server (the client sending the file) as a viewpoint. For the client, the transfer direction is simply reversed.

```
--- Server ---

Repeat until init signal

<- PING
-> REPLY

---

Actual protocol

<- init
-> file name
<- "Received Name"
-> file size
<- "SIZE:FILE_SIZE" or "NONEXISTANT" (determines whether the file transfer should start from scratch or resume where it was interrupted)
-> "Ready for transfer!"
<- "Beginning file transfer..."
-> file data
<- "GIVE_ME_HASH"
-> FILE_HASH
```
