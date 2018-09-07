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

Let's try to make it asynchronous. The best way, if you can't modify the code, is to wrap the method into an asynchronous Mono (or Flux) and call it within a ```flatMap()```.

> Note: the wrapper method already exists, you simply have to uncomment the flatMap line and comment the doOnNext line in the save method.

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
* Notice that the order in which beverages are effectively created has been modified compared to the previous scenario. Vittel is now created first because it takes no time to taste it. Why? Because 'save' is asynchronous and ```flatMap()``` doesn't care about initial order.

### 4. Run the taste method on an elastic scheduler

We could consider that the save service is actually fine, except for the tasting. So we could choose to let 'save' run on the processing thread, but run the 'taste' part on a different scheduler.

To do this, undo the previous step and redo it again only on the taste method.

> Note: again, name your scheduler. Make sure you use only one.

> Note: if you succeeded and like challenges, try to to the same with the blocking taste method (so without the changes made at step 2). See why making it asynchronous was a good idea ?

Visible Benefits:

* Not much compared to the previous step. This is mostly another design option where only the bottleneck is set on a different scheduler.

> Note: Usually it is a good practice to stay agnostic on concurrency within a method, so the consumer can decide if it must run on a scheduler or not (using subscribeOn).

> However, when the method is known to be slow by design, it might be a good idea to force it always on its own scheduler (using publishOn), just like the reactive MongoDB driver did (see above).

### 5. Refactor the taste method to be non-blocking

Currently, the taste method, even if asynchronous, is just a wrapper around ugly code that blocks the thread. What if we have access to the code and can rewrite it in a non-blocking way?

Reactor has an operator ```delayElements``` that delays any event for a given time, which is exactly what we want to do. By default, it is scheduled on the parallel scheduler, which has a fixed number of threads equal to the number of CPU cores (2 in my case).

Try to comment the current implementation of ```taseAsynchronously()``` and uncomment the alternative implementation. You can also remove any scheduler used at step 4.

Visible Benefits:

* The process runs just as smooth but with less threads. Using an elastic scheduler is not necessary anymore
* It doesn't look very spectacular, but think about repeating the initial sequence of beers a thousand times (making 6000 beers to taste :-| ). Then you can probably measure the impact on memory and thread consumption.

> Note: this is exactly what happens when you replace the classical MongoDB driver by the reactive, non-blocking driver. Wrapping the classical driver into reactive code is ok, but it's still blocking threads.

### 6. Keep the initial order of beers

What if the order of beers in the initial sequence was important? Due to ```flatMap``` operator subscribing eagerly to every sub-publisher and simply merging events as they come, the initial order is currently lost.

Which other operator could you use to guarantee the beers are created following their initial order? What is the impact on concurrency?

> ```concatMap``` is the operator that guarantees the initial order, because is subscribes to sub-publishers sequentially. The consequence is that concurrency is lost.     

> ```flatMapSequential``` seems a good idea at first, but it subscribes eagerly to all sub-publishers, just like ```flatMap``` does. Only the output is set back to the initial order.

So we cannot have both concurrency and initial order by just replacing an operator by another one. It looks however possible to change the logic and the code in order to (for example) taste all the beers in parallel but save them sequentially.
This needs a bit of refactoring though. Don't hesitate to have a try ;-)


 


