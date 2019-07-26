# HOW TO RUN :
  -first run PubSub.java
  -then run workerMain.java
  -Enter IP and PORT ( ip:127.0.0.1 , port: 19999)
  -Run client enter (the same ip and port)
  ( there is a test in which 100 clients will be asking for worker )
  - most of the changes that I made was related to RL in QlearningLoadBalancer class , Policy class and  ....
# Reinforcement Learning
In each epoch performance is observed , some workers and clients are run and
according to this observation Q tabel is updated reward is calculated then the new action allocates resourses to workers
<img src=images/image3.png" width=450 > 
