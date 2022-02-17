# SteamInventoryHistoryV2
 Attempt 2 at an inventory analyzer
 
 ## Compiling
 TODO
 
 ## How to use
 First up you need to download your inventory history.
 ### Downloading your inventory history
 Go to https://steamcommunity.com/my/inventoryhistory/?app%5B%5D=440 in google chrome. You can try firefox but my branch doesn't allow to download pages that big so it is untested.
 Open console (F12 -> Console tab) and paste the following.
 ```
 clear = setInterval(()=>{
    if(document.getElementById("load_more_button") != null && window.getComputedStyle(document.getElementById("load_more_button")).display != "none" && window.getComputedStyle(document.getElementById("load_more_button")).visibility != "hidden"){
        InventoryHistory_LoadMore();
        window.scrollTo(0,document.body.scrollHeight);
    }
}, 5000)
```

This will automatically start pressing the "Load more" button at the bottom of the page every 5 seconds, if it exists. When it is finished put the following in console to stop it.
```
clearInterval(clear);
```
## DO NOT RESIZE YOUR BROWSER AFTER THE SCRIPT STARTS. THIS INCLUDES CLOSING CONSOLE.
YOUR BROWSER WILL FREEZE IF YOU HAVE LOADED ENOUGH ROWS AND YOU WILL HAVE TO REPEAT THE PROCESS IF YOU DID NOT SAVE THE PAGE YET.

Note: Read the entire next paragraph before following its instructions.
From my experience this will load your inventory back a few pages and then it will stop. You will then have to manually jump to the day before it stopped (for example: if it stopped on a trade at November 11, 2021 you will have to go to the top of the page and jump to date November 10, 2021). This page will take extra time to load as I presume you are accessing uncached data. You can either save the page before you go back a day (in the example it will have your inventory history from sometime during November 11, 2021 to the current date), and then after you load the previous day you can jump to the day after the original date you stopped (in the example this would be November 12, 2021) and resume the script from there OR you can attempt to start over again and only have 1 page. I would not recommend the 2nd method if you have a large history and I did not test it so it may not even work.
Alternatively this was a 1-off thing for me and your entire history will load in the first go.

This process may take a long time, it took about 3 hours for myself and I ended up with 2 HTML files about 140mb combined due to having been playing since 2014 with ~45,000 inventory events. If you come back to it being done with a gray overlay it means you ran into an unknown error and you can close it by clicking on the page. The page itself required slightly less than 4.5 gb of ram so if you don't have that much you may want to consider doing periodic downloads by inputting the stop command, saving the page, then going to the day after where you stopped. It should be noted this program cannot differentiate anything less than a minute so if you think you stopped in the middle of a big SCM purchase or unboxing spree you may want to continue hitting the load button until you stop between events that took place 1 minute apart or else you have duplicate or missing information.

After you have your HTML files you can copy/drag them into a folder named 'html' that is in the same folder as the jar file. NOTE: running the jar will create the html folder for you and will wait for you to paste the files before continuing. You can delete the folders that come with downloading the webpage, they mostly contain images for the items and will not be used by the program.

### Actually running the jar
TODO