# TDFix：fix deadlock based on template
---  
> We propose **TDFix**, a template-based tool for fixing deadlocks. It is an end-to-end tool which contains deadlock detection, localization and fixing. Thus, our tool is closer to the practical software debugging. Experimental results show its significant effectiveness and efficiency. 

# 1. Overview
--- 
![overview](https://github.com/jiwangjie/TDFix/assets/74883729/d4ae7a30-0f70-45d1-9fb5-e862f66d3061)


**We divide the functioning of TDFix into four phases:** 
+ **(a) Constructing Templates.**
  We analyzed a large number of deadlocks caused by ReentrantLock and Synchronized and their corresponding fixed programs to summarize the rules.
  
+ **(b) Constructing AST.**
  AST can provide an efficient form of representing program structures to reflect the syntax problems. Therefore, we build an AST for programs that need to detect and fix deadlocks.
  
+ **(c) Detecting Deadlocks.**
  We use the template we built earlier to detect deadlocks in our AST. For the deadlock caused by ReentrantLock, we analyze whether the number of locks and unlock nodes is equal for locks of type java.util.concurrent.locks.ReentrantLock in the AST of the program. In sequential and loop structures, our approach calculates whether the number of locks and unlocks is consistent, while in branch structure, we check whether there is an unlock operation in each branch. For deadlocks caused by Synchronized, we analyze whether there is a phenomenon of waiting forever in the AST of the program.
  
+ **(d) Locating and Fixing Deadlocks.**
  Locating and Fixing Deadlocks. For deadlock caused by ReentrantLock, localization is a process of matching locking and unlocking of ReentrantLock. The fix is to add the unlock node at the appropriate place in the program AST. However, for deadlocks caused by Synchronized, localization is a process of analyzing the location of synchronized code blocks where a wait occurs between threads. the fix is added a gate synchronized node outside the two lock acquisition statements in the program AST.

# 2. Templates
--- 
**We analyzed a lot of cases and built some templates.**
![image](https://github.com/jiwangjie/TDFix/assets/74883729/453924b3-b8ea-4b0f-a61e-d1d0cb7f825e)


## AST for template
---  
### template 1:
> <img src="https://github.com/jiwangjie/TDFix/assets/74883729/edb2253e-e7a6-4bb6-bc0e-d89cfc542c59" width=400/>

> <img src="https://github.com/jiwangjie/TDFix/assets/74883729/83481cb9-d887-4f70-a13f-18c3cca6d83e" width=600/>


### template 2:
> <img src="https://github.com/jiwangjie/TDFix/assets/74883729/096e8433-6ae6-4d32-a02e-13dc8c8d5ab2" width=400/>

> <img src="https://github.com/jiwangjie/TDFix/assets/74883729/d4895dfc-fb06-431f-8324-b9b63a61779e" width=600/>

### template 9:
> <img src="https://github.com/jiwangjie/TDFix/assets/74883729/e1946ff9-f7a6-4462-afc5-9af5c72074a7" width=400/>

> <img src="https://github.com/jiwangjie/TDFix/assets/74883729/8bcbbd5f-624d-4172-abec-e5267a1c014d" width=600/>


### (More AST templates are being uploaded...)

# 3. ReentrantLock example
> ![image](https://github.com/jiwangjie/TDFix/assets/74883729/df721c06-aa7f-452b-b49e-999079524b35)

# 4. Results
--- 
![image](https://github.com/jiwangjie/TDFix/assets/74883729/2e3fb6d9-6945-4171-a70d-41252923d23a)


# How to use our tool?
---  
## prepare

> 1.java 1.8

> 2.gradle 7.0

> 3.clone our source code to you computer.

## running(To illustrate in more detail how our tool is used, we use the IntelliJ IDEA 2023.1.3 tool demo.)

### 1.open project using idea.
![image](https://github.com/jiwangjie/TDFix/assets/74883729/52dd65b3-1125-478f-a84d-5e61d9fcdad5)

### 2.you will got this
![image](https://github.com/jiwangjie/TDFix/assets/74883729/62cc39a2-3002-46e6-9156-f9e58f71845d)

### 3. config gradle and jdk and apply
![image](https://github.com/jiwangjie/TDFix/assets/74883729/e1023717-c5fe-484c-8681-eafeaa23a79d)

### 4.Place deadlocked java code in the app\src\main\java\repairer folder.
![image](https://github.com/jiwangjie/TDFix/assets/74883729/74d5855e-9143-402c-9e57-db4718f54ca3)

### 5.config build.gradle file.(buildSrc\src\main\java\vifim\repairer\Recipe folder to place our fix template)
+ (1)Locate the configuration file **"build.gradle"**.
+ (2)Activate the template you want to use, which corresponds to the Recipe in buildSrc.
+ ![image](https://github.com/jiwangjie/TDFix/assets/74883729/2fd43d52-3b84-424e-b686-d4fbe8679c3b)

### 6.start TDFix
![image](https://github.com/jiwangjie/TDFix/assets/74883729/814769ab-905c-4536-bf87-3b0dd7994760)

### 7.Another way to start.

+ (1)Locate the the root of the project on the terminal.

+ (2)Enter the running command **".\gradlew rewriteRun"**.

### 8.You can see the time it took TDFix to fix the deadlock in the console. In addition, we will not keep a backup of java files that contain deadlocks, and will fix deadlocks directly on java files that contain deadlocks.
![image](https://github.com/jiwangjie/TDFix/assets/74883729/f79079cf-7102-4312-b256-93efbce03774)


### 9.add your custom template?

+ (1) Locate the floder **"buildSrc\src\main\java\vifim\repairer\Recipe"**.

+ (2) add you template.(You can use openrewrite to write your own templates.)

+ (3) Locate the file **"\app\build.gradle"**.

+ (4) open your template
```
rewrite {
	activeRecipe(
		"vifim.repairer.Recipe.LockCheckForLoopRecipe",
		// "vifim.repairer.Recipe.YourTemplate",
	)
	// This is the value of configFile. It is not necessary to specify this value
	configFile = project.getRootProject().file("/buildSrc/src/main/resources/META-INF/recipes/repairerRecipes.yml")
}
```

## Discussion  

Our summarized templates are for frequent deadlock cases and cannot cover all deadlocks, there are still 22 deadlock cases in the dataset that cannot be fixed. Next, We will provide some examples and explanations.

---  
**Case Code:**  

```
public V take() throws InterruptedException {
    final ReentrantLock lock = this.lock;
    lock.lockInterruptibly();
    try {
        for (;;) {
            Task t = queue.peek();
            if (t != null && (!ordered || index.compareAndSet(t.id, t.id + 1))) {
                queue.poll();
                return t.result;
            }
            available.await();
        }
    } finally {
        //  lock.unlock();
}
```
**Explanation**  
This code is from the project Camel. It defines a method to retrieve task objects from an ordered task queue, implementing t  he following functions:
Under the protection of a ReentrantLock lock, the loop checks the head element of the queue, if it is not empty and the order requirement is met (i.e., the id and index of the task are equal), remove the element from the queue and return its result. If the queue is empty or does not meet the order requirement, wait for the notification of the available condition variable, indicating that a new task has been added to the queue or a task has been executed. At the end, the lock is released whether it is a normal return or an abnormal exit.
If lock.unlock() is missing, this code is possible deadlock. In this case, each thread does not release the lock after it executes take(), but instead holds the lock forever. As a result, other threads cannot acquire the lock or execute the take() method and have to wait. If more than one thread calls the take() method at the same time, it creates a loop waiting situation, leading to a deadlock.
In our approach, when traversing the AST corresponding to the procedure, we determine whether there is a lock acquisition operation in the program by checking whether the name of each method call node contains lock(). lock.lockInterruptibly() is not recognized by our approach as a lock acquisition operation, so our approach cannot detect a deadlock in case code and fix it.

--- 
**Attention**  
Our approach places the unlock at the end of the method. **This strategy not only avoids deadlocks and memory leaks, but is consistent with situations where a developer might choose to deliberately unlock a piece of code. However, if developers want to unlock resources earlier, it may sacrifice some performance.** In the future, we'll try to find a more precise location of unlock.
