[![https://jappieklooster.nl](https://img.shields.io/badge/blog-jappieklooster.nl-lightgrey?style=for-the-badge)](https://jappieklooster.nl/my-thesis.html)

This is a chatbot that can have varied respones depening on personality.
Personality is implemented as a process.
This is an evolution upon `salve' architecture which was based upon the
[[http://www.alicebot.org/][ALICE chatbot]],
we moved away from this based to a yaml dialogue modeling scheme.
In this sceme we encode utterances as `symbols', and the transition between
utterences are `connections'.
On top of this base idea we added various mechanisms of letting the chatbot
decide which connections to take, such as `perlocutionary values' or `goals'
amongst other things.

The core of the system is the [drools rule engine](https://www.drools.org/),
because of this chatbot is incredibly flexible.
You can for example quite easily create a rule that detects if the user repeats
himself and setup a scene to handle that.
Or you can create timer rules, letting your bot pretend to be impatient.
I was sceptical at first about this technology, its a little clunky
(little IDE support for example),
but this engine has some real potential.
Making this rule engine the center of the chatbot is a big result of my thesis
and my prime reason for putting the source online,
I know there are people that want this even though they don't know it yet.

Pattern matching is done inside the drool rule engine too.
This means the pattern matching mechanism is not part of the modeling scheme
such as in ALICE,
this opens up a legion of possibilities to do clever pattern matching tricks.
Think for example a chatbot that does pattern matching with statistical
approaches, machine learning or information theory.

To help people move away from legacy AIML we wrote a conversion script.
This should work for most cases and otherwise is easily adaptable (254 lines).

We refer to the thesis for an extended description,
or for programmers I wrote a blog post.

# Executing
[Install a jdk](http://openjdk.java.net/install/index.html), then to run you can use gradlew:

```sh
./graldew run
```

Or if you have gradle you can replace that with gradle
(if you don't have gradle just use gradlew it's rocksolid).

The application is already embeded into a website,
however you could make swing based clients relativly easily.

# TODO todo
+ Update readme
  + Difference from ALICE (both images)
  + The mapping with connections
  + Add links to my website blogpost
  + Add links to my thesis
+ Fix the horible client layout.
  + Could make an auto logon (on page load)
  + Enter text below and messages that are older moving up
    (it just seems unatural pushing older messages down.)
+ Add citations (for salve game for example)
+ Add thesis (once completed)

## Done
 + ScalaJS + Akka http in gradle
 + copied source code over from old maven build
 + Connect akkahttp server to drools chatbot
 + Delete drools about sp and such (Not really used, also copyrithgt stuff)
   + Make sure git fame says pracitcally 0 lines are from manuel
   + Copy to a new repository so that we don't commit infringement on history

# License
The reference project is MIT, because I modified [this project](https://github.com/jrudolph/akka-http-scala-js-websocket-chat).
The chatbot is licensed under LGPLv2,
this allows it to be used as a dynamic library while still urging people to
contribute back.
With this license you can also add custom rules that are not under this license
(because kie can load rules at runtime, and lgpl has an exception for runtime
coupling),
this will make it easier to use this project commercially.
Finally the conversion script is licensed under GPLv2,
I hope people will share their contributions to this script
(but once ran you no longer need it).

If your project requires different licenses please contact [me](https://jappieklooster.nl).
