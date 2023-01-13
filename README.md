# Easy File Transfer Tool

This app can be used to transfer files easily between two hosts on any operating system. 

*This app is specifally designed for Android, see below for other operating systems.*

It is the companion app to the python script [ptransfer](https://github.com/leolion3/Portfolio/tree/master/Python/FileSender)

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
