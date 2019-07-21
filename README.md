# TCP Fiability Tester
Scala JavaFX application for TCP connection fiability

In test creation tab, you specify the node, the duration, the interval of each record, and the timeout in which the connection must be established.

### Get Started

You will need **Java** and **openjfx** installed on your machine

```bash
# Run privileges 
chmod +x ./bin/tcp-tester.sh
# Run it
./bin/tcp-tester.sh
```

### Create Test

![Alt text](https://media.giphy.com/media/McD7aBXafuIkhuaHEc/giphy.gif "Table of tests")

Duration input can be: [0-9]* (second|minute|hour)s?

Interval input can be: [0-9]* (second|minute|hour)s?

Timeout input can be: [0-9]* (millisecond|second|minute|hour)s?

### Sequence Diagram

![Alt text](https://ibin.co/w800/3SK3wcRHnCct.jpg "Sequence diagram")


### Useful Links

[TCP Connection Establishment](https://en.wikipedia.org/wiki/Transmission_Control_Protocol#Connection_establishment)

[Mean time between failures](https://en.wikipedia.org/wiki/Mean_time_between_failures)

