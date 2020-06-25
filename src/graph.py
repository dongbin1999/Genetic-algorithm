import numpy as np
import matplotlib.pyplot as plt
x=np.arange(0,40)
y=x+1
fig = plt.figure(figsize=(12,7))
ax = fig.add_subplot(1,1,1)
ax.set_xticks([2.5,6.5,10.5,14.5,18.5,22.5,28,33,38])
ax.set_xticklabels(['Iron','Bronze','Silver','Gold','Platinum','Diamond','Master','Grandmaster','Challenger'])
plt.title('estimated graph', fontsize = 20)
plt.plot(x,y,label='y=x+1',linewidth=2)
plt.legend(fontsize=15)
plt.xlabel('User tier',fontsize = 20)
plt.ylabel('Wards(per 5 games)',fontsize = 15)
x = []
y = []
with open("./데이터.txt","r") as file:
  for i in range(110):
    tmp = file.readline().split()
    x.append(int(tmp[0]))
    y.append(int(tmp[1]))
plt.grid()
plt.scatter(x,y,color='red')
plt.show()
