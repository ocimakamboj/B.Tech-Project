# Bounded Rationality in Service Management

## Introduction 
The traditional economic and queueing literature assume perfect rationality on the part of the decision maker. When a fully rational decision maker is given a set of alternatives to choose from, the rational agent will choose the outcome that gives him the maximum pay off. This benefit that the decision maker gets from choosing a particular alternative is termed as utility. Thus, under the paradigm of perfect rationality, the decision maker optimizes his utility.

A customer considering whether or not to join a queueing system to avail a service is a decision maker. One may ask, does this customer fully and accurately analyze the costs and benefits of joining a queuing system before making a decision? Classical literature assumed that the customers are perfectly rational, and hence they maximize their own utility when faced with such a decision. Recent experimental findings point to the contrary; a decision maker is only boundedly rational. Bounded rationality captures the inherent imperfections in the human decision making process. The idea is that the rationality of decision makers is bounded by the limited amount of information they have, the cognitive limitations of their mind, or the finite amount of time they have to make the respective decision. The term ‘bounded rationality’ was coined by Simon in his book ‘Models of Man’ (1957). Simon (1955) proposed satisficing as a more accurate way to model human decision-making behavior: Rather than optimizing perfectly, agents search over their choice domain until they find a satisfactory solution.

There are many approaches towards modelling bounded rationality. The model that we use is derived from the Quantal Choice Theory, which states that *while the best decision need not always be made, better decisions are made more often*. For mathematical convenience, we choose to use the logit choice model – when faced with a set of alternatives, the probability of selecting any one is directly proportional to the exponential of the utility that one gets from selecting that particular alternative. One more advantage of this model is that it parametrizes the level of bounded rationality of the customer by β. Thus, we incorporate continuous levels of bounded rationality in our model including the two extremes-

1. Full rationality, where the customer perfectly optimizes his utility.
2. Full bounded rationality, where the customer lacks the ability to make an informed decision and randomizes among all the choices available.
    
This work studies the effects of incorporating bounded rationality in traditional queueing and service systems. Specifically, we model the multi class customer, single server pool service system. In this system, the customer observes the state of the system, and based on that decides whether to join the queue or not. This decision is affected by the level of bounds on the rationality of the customer.

The questions that we aim to address are – what are the optimal number of servers that should be employed from a revenue maximizing firm’s perspective, and separately, from a social planner’s perspective. We investigate the impact of bounded rationality on part of the customers.

## The System

Consider a multiple server queuing system with *s* number of servers. Potential customers, who are homogeneously boundedly rational with the level of bounded rationality as &beta;, arrive to this system according to a Poisson process with rate &lambda;<sub>k</sub>, where *k* is the class of the customer. Customers of class-1 are served before class-2, followed by class-3, and so on. 

Upon arriving, they have the option to balk after observing the queue length. A customer who decides to balk receives zero utility. If the customer joins the queue, she pays a price *p* and receives a reward *R* on completion of the service, *R>0*. She also incurs a cost of *C* per unit of time while staying in the system (either waiting or being served). 

If a customer joins the queue, his utility is *U = R-p-CW*, where *W* is the wait time.

Service times are assumed to be independently, identically, and exponentially distributed with mean 1/&mu;<sub>k</sub>, where *k* is the class of the customer. Customers within a class are served on first come first serve basis. Upon arrival, after observing the customers in the system, each customer decides to join the queue with the following logit probability.

&psi; = exp(u/&beta;)/(1+exp(u/&beta;))

## Codes

The repository contains the codes for M/M/1 queue, M/M/s queue, Priority queue, and Priority Queues with full and Bounded Rationality respectively. \
We experiment with different ways of calculating the wait time for estimating the utility of a customer.

You can start with running 'TestV1.java'. 
