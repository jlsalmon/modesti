modesti-api
===========

Monitoring Data Entry System for Technical Infrastructure (backend API)

Development
-----------

Requirements for running backend:
* MongoDB 2.6.10+ running
* Connection to TN (for C2MON and TIM connections) or appropriate tunnels

Instructions:
* Clone the repo: `git clone ssh://git@gitlab.cern.ch:7999/modesti/modesti-api.git`
* Open the project in IDEA/Eclipse (or emacs or whatever)
* Compile and run: `java cern.modesti.Application -Dspring.profiles.active=(test|dev|prod) -Dc2mon.client.cont.url=http://whatever`


Deployment
----------

`TODO`