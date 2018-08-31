# Drink planner sample application

This project has been created to illustrate presentations on Reactive Programming. It can be used by anyone as a sandbox to experiment with 
Spring Boot 2, Reactor 3 and other related technologies.

Slides of the presentation can be found here:
* Part 1 - the basics: https://odume.github.io/slides-reactive-programming-basic
* Part 2 - advanced: https://odume.github.io/slides-reactive-programming-advanced

Videos of the presentation can be found here:
* Part 1 - the basics: https://web.microsoftstream.com/video/ec8ba50f-bded-4441-9f17-dc996750240b
* Part 2 - advanced: https://web.microsoftstream.com/video/0987b1f1-d1a6-405a-84b5-97f021a83b3c

## Design

It is a Spring Boot 2 application, built with Gradle.  It relies on a MongoDB database and a Redis cache. They both need to be available on localhost for the app to start correctly.

The code follows a standard DDD structure, where the models, the repositories, the services and the REST controllers are clearly separated into different packages.

The only model in this case is the ```Beverage``` entity, which is persisted as a Mongo Document by a reactive repository.

A ```CommandLineRunner``` (think of it as a bootstrap script) in ```DrinkPlannerApplication.java``` is executed at the start of the application to create a few hard-coded beverages in a reactive way.

In addition, a REST controller publishes a list endpoint and a retrieve endpoint (based on the id). Both endpoints are reactive from top to bottom.

## How to run

The best option is probably to fork this repository in Innersource, then clone it and import it into IntelliJ or any IDE capable of running Spring boot applications (STS).

The application will need both MongoDB and Redis running on localhost in order to start correctly. It is highly recommended to run them as docker containers to avoid installation procedures:
```
docker run -p 27017:27017 --name mongo mongo:latest
docker run -p 6379:6379 --name redis redis:latest 
```

> Note: IDE plugins like 'Docker Integration' in IntelliJ can be a good help to setup and start these containers. 

> Note: It will be helpful to have some easy way to clean the mongodb content from time to time. Again, some IDE plugins can help.

Now you can start the application, either from the IDE or using the gradle command if you like (in the root directory)
```
gradlew bootRun
```

> Note: running it from the IDE gives you the option to use the debugger

You should be able to see the logs from the ```CommandLineRunner``` creating the beverages.

## What to do

The current state of the application is sufficient to understand how things are working with Reactive Programming. It is possible to use breakpoints, add logs, add operators or even your own services or endpoints if you like. See it as a sandbox

In addition, you will find a branch with some modifications regarding concurrency, one of the advanced topics.
Once you've acquired a pretty good understanding of how the application works in its current state, don't hesitate to look at the following 'exercises' to apply concurrency.
Every commit in the branch is related to one exercise, applied in the same order. The description of the commit contains the same numbers as below so they can be easily related to an exercise.
Also, you will find in the ```/logs``` sub-directory the related logs you should have when running the app after the change.

### 1. Run the script on a different thread

As you can notice when running the app, the whole script from the CommandLineRunner is executed on the same thread (the main thread), except for the parts related to the MongoDB reactive driver which happens on a dedicated thread (ntLoopGroup-2-*).
The whole thing takes ~4s to run (on my machine :-))

Let's try to run it on a dedicated thread instead, and see how it behaves.

> Note: There are two ways to perform this change:
> * use ```subscribeOn``` if you consider that the whole Publisher is slow
> * use ```publishOn``` if you consider that the ```save``` consumer is slow
>
> You can try both and see the differences. I prefer the subscribeOn way in this case.

Try to name your scheduler so it is clearly identifiable in the logs.

What type of scheduler will you use for this script? Elastic? Parallel? Other?

> In the end, whatever the solution you chose, you will see that the whole script is indeed run on a different thread, but it is still a single thread.
>
> Why? Well, simply because we didn't tell Reactor to do something else... yet. We just said *'run the main publisher on this thread'*, not 'execute every item on a different thread' 

Visible Benefits:

* See how the main thread is released after a few milliseconds (17 in the example log). If you think that this main thread could have been a REST request instead, it means that the thread can now serve another request after 17ms, while it was blocked for 4s before the change.
* However, the whole execution still takes ~4s, because everything still happens on the same thread

### 2. Make the taste method asynchronous

Clearly, the bottleneck is the taste method, which takes a lot of time to execute depending on the alcohol rate of the beverage.
In its current version, it is synchronous and blocking, and invoked from a ```doOnNext()``` operator.

Let's try to make it asynchronous. The best way, if you can't modify the code, is to wrap the method into an asynchronous Mono (or Flux) and call it within a ```flatmap()```.

> Note: the wrapper method already exists, you simply have to uncomment the flatmap line and comment the doOnNext line in the save method.

Visible Benefits:

* Not much. The fact we made the tasting asynchronous for every beverage is a good thing for next changes we will perform, but doesn't have a visible effect so far since everything is executed on the same thread. 
* Enabling asynchronous calls is not sufficient to enable parallelism or concurrency. In this case, the same thread is still blocked while tasting every beverage one by one.

### 3. Run the save method on an elastic scheduler

The save service is already asynchronous (it returns a Mono). Lets make it run on yet another scheduler, elastic this time.

> Note: try to name your scheduler to be able to differentiate it from the 'process' one.
> Pay attention to use the same scheduler for all beverages in the flux, and not to create a new scheduler for each of them.

> Note: again it is possible to do this with both publishOn and subscribeOn. Compare both and see the differences. 2 distinct commits have been done with each solution.
>
> Consider the impacts from a design perspective: using publishOn will impact all the consumers of the 'save' service (which might be a good thing, this is what the MongoDB driver is doing). Using subscribeOn lets the consumer decide if it wants to run it on a different thread or not (which might also be a good thing).
>

Visible Benefits:

* Now we clearly declare that the calls to 'save' for every item should be performed on a thread pool, using one thread per beverage. This clearly enables parallelism. The whole process now takes ~1,2s to complete, which is the time it takes to taste a Bush.
* Notice that the order in which beverages are effectively created has been modified compared to the previous scenario. Vittel is now created first because it takes no time to taste it. Why? Because 'save' is asynchronous and ```flatmap()``` doesn't care about initial order.

### 4. Run the taste method on an elastic scheduler

We could consider that the save service is actually fine, except for the tasting. So we could choose to let 'save' run on the processing thread, but run the 'taste' part on a different scheduler.

To do this, undo the previous step and redo it again only on the taste method.

> Note: again, name your scheduler. Make sure you use only one.

> Note: if you succeeded and like challenges, try to to the same with the blocking taste method (so without the changes made at step 2). See why making it asynchronous is a good idea ?

 
 


