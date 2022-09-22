import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;



public class ConvexHullPara2 extends ConvexHull{
    public int n;
    public int[] x, y;
    public int MAX_X, MAX_Y, MIN_X;
    int threadCount;
    volatile int threadId = 0;
  
    /* The list that represents our points. It's simply a list of
    integers that references indexes to the x and y arrays.
    The x and y arrays hold the coordinates of our points. */
    public IntList points;

    public ConvexHullPara2(int n, int seed, int threadCount) {
        super(n, seed);
        //this.n = n;
        x = new int[n];
        y = new int[n];
        this.threadCount = threadCount;
    
        NPunkter17 np = new NPunkter17(n, seed);
        np.fyllArrayer(x, y);
        points = np.lagIntList();
      }
      public static void main(String[] args) {

        int n, seed;

        try {
    
          n = Integer.parseInt(args[0]);
          seed = Integer.parseInt(args[1]);
          
    
        } catch (Exception e) {
    
          System.out.println("Correct usage is: java ConvexHullPara2 <n> <seed>");
          return;
    
        }
        int threadCount = Runtime.getRuntime().availableProcessors();

        if(n<=200){
            ConvexHull ch = new ConvexHull(n, seed);
            IntList convexHullSeq = ch.quickHull();

            ConvexHullPara2 chp = new ConvexHullPara2(n, seed,threadCount);
            
            IntList convexHull = chp.quickHullPar(n);
            System.out.println("ConvexHull Seq: ");
            convexHullSeq.print();
            System.out.println("ConvexHull PAR: ");
            convexHull.print();


            Oblig5Precode op = new Oblig5Precode(ch, convexHullSeq);
            op.drawGraph();
            op.writeHullPoints();
        }
        else{

        
        long t = System.nanoTime();
        double tid = (System.nanoTime()-t)/1000000.0;
        double [] tSeq = new double [7];
        double [] tPar = new double [7];
        double [] tOldPar = new double [7];
        // ------------ paralel part
        for (int i = 0; i < tOldPar.length; i++) {
            ConvexHullPara2 chp = new ConvexHullPara2(n, seed,threadCount);
            t = System.nanoTime();
            IntList convexHull = chp.quickHullPar(n);
            tid = (System.nanoTime()-t)/1000000.0;
            tPar[i] = tid;
            
        }
        Arrays.sort(tPar);
        System.out.println("PAR TIME: "+ tPar[7/2]+" ms");
        // System.out.println("Par: ");
        //convexHull.print();
        // -----------------------------------------------
        for (int i = 0; i < tOldPar.length; i++) {
            ConvexHull ch = new ConvexHull(n, seed);
            t = System.nanoTime();
            IntList convexHullSeq = ch.quickHull();
            tid = (System.nanoTime()-t)/1000000.0;
            tSeq[i] = tid;
            
        }
        Arrays.sort(tSeq);
        System.out.println("SEQ TIME: " + tSeq[7/2]+ " ms");
        System.out.println("SPEEDUP: " +tSeq[7/2]/tPar[7/2]+" ms");
     }
       
        // System.out.println("Checking if convexHull lists match...");
        // checkIfHullsMatch(convexHull, convexHullSeq);
        if(n==1000){
            ConvexHull ch = new ConvexHull(n, seed);
            IntList convexHullSeq = ch.quickHull();
            Oblig5Precode op = new Oblig5Precode(ch, convexHullSeq);
            //op.drawGraph();
            op.writeHullPoints();
        }
      }
      
      public static void checkIfHullsMatch(IntList ch1, IntList ch2){
          for (int i = 0; i < ch1.len; i++) {
                int left = ch1.get(i);
                int right = ch2.get(i);
              if( left != right){
                    System.out.println("Not Equal!");
                    break;
              }
          }
          System.out.println("Lists match!");
      }
      public static void joinThread(Thread t1) {
        try {
            t1.join();
        }
        catch (InterruptedException  e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();
            return;
        }
    }
      public IntList quickHullPar(int num) {
        /* Find any two points we know are on the line. Here we choose the points
  with the maximum and minimum x coordinates */
  
            for (int i = 0; i < points.size(); i++) {
      
                 if (x[i] > x[MAX_X])
                   MAX_X = i;
                 else if (x[i] < x[MIN_X])
                   MIN_X = i;
      
      
            /* This is just for use in the precode,
            and is not part of the actual algorithm */
                 if (y[i] > y[MAX_Y])
                   MAX_Y = i;
          }
        

    /* Create our list in which we store the points in the convex hull */
    IntList convexHull = new IntList();
    IntList convexHullRight = new IntList();
    IntList convexHullLeft = new IntList();
    
    Thread t1 = new Thread(new ParWorker(MIN_X, MAX_X,convexHullRight, points, 2, threadId++));

    Thread t2 = new Thread(new ParWorker(MAX_X, MIN_X, convexHullLeft, points, 2, threadId++));

    t1.start();
    t2.start();
    joinThread(t1);
    joinThread(t2);

    convexHull.add(MAX_X);
    convexHull.append(convexHullRight);
    convexHull.add(MIN_X);
    convexHull.append(convexHullLeft);
 

    return convexHull;



    }
    class ParWorker implements Runnable {

        int p1,p2;
        IntList container;
        IntList applicablePoints;
        int level;
        int threadId;

        
        public ParWorker(int p1, int p2,IntList container,
                                              IntList applicablePoints,
                                              int level,
                                              int threadId) {
            this.p1 = p1;
            this.p2 = p2;
            this.container = container;
            
            this.applicablePoints = applicablePoints;
            this.level = level;
            this.threadId = threadId;
        }
        @Override
        public void run() {
           // System.out.println("TH-id: " + threadId);
            findPointsToRightPara2(p1,p2, container, level, applicablePoints);

            
        }
        public void findPointsToRightPara2(int p1, int p2, IntList container,  int level, IntList points){
            int a = y[p1] - y[p2];
            int b = x[p2] - x[p1];
            int c = (y[p2] * x[p1]) - (y[p1] * x[p2]);
        
            int maxDistance = 0;
            int maxPoint = -1;
            int maxPoint2 = -1;
        
            /* Use to store all the points with a positive distance */
            IntList pointsToLeft = new IntList();
            IntList pointsOnLine = new IntList();
            boolean flaggOnLine = false;

            for (int i = 0; i < points.size(); i++) {

                /* Getting the index of the point */
                int p = points.get(i);
          
                /* Calculating the 'distance' to the line point1 --> point2.
                The actual distance is (ax + by + c ) / squareroot(a^2 + b^2).
                However, the denominator of the fraction only scales down the distance,
                and since we are only interested in the distance relative to the other
                points, we can exclude that calculation. */
                int d = a * x[p] + b * y[p] + c;
          
                if (d > 0) {
          
                  pointsToLeft.add(p);
          
                  if (d > maxDistance) {
                    maxDistance = d;
                    maxPoint = p;
                  }
                }
                else if (d == 0 && p != p1 && p != p2){
                  pointsOnLine.add(p);
                  maxPoint2 = p;
                  flaggOnLine = true;
                }
                
              }
              if (maxPoint >= 0) {
                  if(Math.pow(2, level) <= threadCount){// Math.pow(2, level) <= threadCount
                    IntList threadContainer = new IntList();
                    Thread newThreadBranch = new Thread(new ParWorker(maxPoint, p2,threadContainer, pointsToLeft,  level + 1, threadId++));
                    newThreadBranch.start();
                    IntList OldThreadContainer = new IntList();
                    findPointsToRightPara2(p1, maxPoint, OldThreadContainer, level, pointsToLeft);
                    joinThread(newThreadBranch);
                    container.append(threadContainer);
                    container.add(maxPoint);
                    container.append(OldThreadContainer);

                  }
                  else{
                      IntList pointsRecursion = new IntList();
                      //IntList pointsRecursion2 = new IntList();
                      findPointsToLeft(maxPoint, p2, pointsToLeft, pointsRecursion);
                        pointsRecursion.add(maxPoint);
                      findPointsToLeft(p1, maxPoint, pointsToLeft, pointsRecursion);
                      container.append(pointsRecursion);

                  }
              }
              else if(flaggOnLine){
      
                container.append(sortPointsOnSameLine(pointsOnLine, p1, p2));  
              }



        }
        
    }

}
