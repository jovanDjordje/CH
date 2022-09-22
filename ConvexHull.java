/**
 * @author Shiela Kristoffersen
 *
 * This finds the convex hull for a set of points. However, if there are several
 * points on a line, then it doesn't include all those points. Including all
 * those points (in the correct order) is a task for you ;).
 *
 * However, if you find it hard, start parallelizing and then come back to
 * it later :).
 *
 * The convex hull is drawn counter clockwise, starting at the point that has
 * the highest x value.
 */
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;

public class ConvexHull {

  public int n;
  public int[] x, y;
  public int MAX_X, MAX_Y, MIN_X;

  /* The list that represents our points. It's simply a list of
  integers that references indexes to the x and y arrays.
  The x and y arrays hold the coordinates of our points. */
  public IntList points;


  public ConvexHull(int n, int seed) {
    this.n = n;
    x = new int[n];
    y = new int[n];

    NPunkter17 np = new NPunkter17(n, seed);
    np.fyllArrayer(x, y);
    points = np.lagIntList();
  }

  public static void main(String[] args) {

    int n = Integer.parseInt(args[0]);
    int seed = Integer.parseInt(args[1]);

    ConvexHull ch = new ConvexHull(n, seed);

    IntList convexHull = ch.quickHull();

    convexHull.print();

    Oblig5Precode op = new Oblig5Precode(ch, convexHull);

    op.drawGraph();
    op.writeHullPoints();
  }

  public IntList quickHull() {

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

    /*
    Here we start our recursive steps.
      1. First we add the point with the largest x coordinate to the convex hull
      2. Then we find all the points to the left of the line MIN_X -> MAX_X
      3. Then we add the point with the smallest x coordinate to the convex hull
      4. Lastly, we find all the points to the left of the line MAX_X -> MIN_X
    */
    convexHull.add(MAX_X);
    findPointsToLeft(MIN_X, MAX_X, points, convexHull);
    convexHull.add(MIN_X);
    findPointsToLeft(MAX_X, MIN_X, points, convexHull);

    return convexHull;

  }



  /*
  This method does two things:
    1. Finds all the points to the left of the line point1 --> point2 and
       stores them in 'pointsToLeft'. This 'pointsToLeft' is then sent in
       as points to the next recursive call. This is done to decrease the
       number of points we have to look through.
    2. Finds the point 'maxPoint' furthest to the left of the line
       point1 --> point2. This 'maxPoint' is part of the convex hull.
  */
  void findPointsToLeft(int point1, int point2, IntList points, IntList convexHull) {

    int a = y[point1] - y[point2];
    int b = x[point2] - x[point1];
    int c = (y[point2] * x[point1]) - (y[point1] * x[point2]);

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
      else if (d == 0 && p != point1 && p != point2){
        pointsOnLine.add(p);
        maxPoint2 = p;
        flaggOnLine = true;
      }
      
    } // end for loop
   
    if (maxPoint >= 0) {
      findPointsToLeft(maxPoint, point2, pointsToLeft, convexHull);
      convexHull.add(maxPoint);
      findPointsToLeft(point1, maxPoint, pointsToLeft, convexHull);
    }
    else if(flaggOnLine){
      
      convexHull.append(sortPointsOnSameLine(pointsOnLine, point1, point2));  
      
    }
  }

public IntList sortPointsOnSameLine(IntList line, int p1, int p2){

  Integer[] val = copy(line);

  if(x[p1]>x[p2]){
   
  //Arrays.sort(val, 0, line.size(), ((Integer i, Integer j) -> ((a1 * x[i] + b1 * y[i] + c1) + (a1 * x[j] + b1 * y[j] + c1))));
    Arrays.sort(val, 0, line.size(), ((Integer i, Integer j) -> ( x[i] - x[j])));      

  }else if (x[p1]==x[p2]){
      if(y[p2]<y[p1]){
        Arrays.sort(val, 0, line.size(), ((Integer i, Integer j) -> ( y[i] - y[j])));      
      }
      else Arrays.sort(val, 0, line.size(), ((Integer i, Integer j) -> ( y[j] - y[i])));      
  }
  
  else Arrays.sort(val, 0, line.size(), ((Integer i, Integer j) -> (x[j] - x[i]))); //(a * x[i] + b * y[i] + c) - (a * x[j] + b * y[j] + c)

  IntList l = new IntList();
  //l.add(p2);
  for (int i = 0; i < val.length; i++) {
    l.add(val[i]);
}
//l.add(p1);
  return l;

}

public Integer[] copy(IntList src) {
    Integer[] val = new Integer[src.size()];

    for (int i = 0; i < src.size(); i++) {
        val[i] = src.data[i];
    }

    return val;
}


}
class Oblig5Precode extends JFrame{
	ConvexHull d;
	IntList theCoHull;
	int n;
	int [] x,y;
	Graph grafen;
	int size , margin;
	double scale ;

public Oblig5Precode(ConvexHull d, IntList CoHull){
		  theCoHull = CoHull;
		  this.d =d;
		  x = d.x;
		  y = d.y;
		  n = d.n;
			size = 500;	// will probably need adjusting depending on your n and seed in NPunkter17
			margin = 50;	// will probably need adjusting depending on your n and seed in NPunkter17
			scale =size/x[d.MAX_X] +0.8;
	}

	public void drawGraph(){
		setTitle("Oblig5, num points:"+n);
		grafen = new Graph();
		getContentPane().add(grafen, BorderLayout.CENTER);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
		// angir foretrukket storrelse paa dette lerretet.
		setPreferredSize(new Dimension(x[d.MAX_X]+2*margin,y[d.MAX_Y]+2*margin));
	}

	public void writeHullPoints(){
		String filename = "CONVEX-HULL-POINTS_" + n + ".txt";

		try (PrintWriter writer = new PrintWriter(filename)) {
			writer.printf("Found %d number of convex hull points in a graph with n = %d:\n______________________________________________________\n\n", this.theCoHull.size(), n);

			for (int i = 0; i < this.theCoHull.size(); i++){
				writer.print("(" + x[this.theCoHull.get(i)] + "," + y[this.theCoHull.get(i)] + ")");
			}

			writer.flush();
			writer.close();
		} catch (Exception e){
			System.out.printf("Got exception when trying to write file %s : ",filename, e.getMessage());
		}
	}

    class Graph extends JPanel   {
	    void drawPoint(int p, Graphics g) {
			     int SIZE =7;
			     if (n <= 50) g.drawString(p+"("+x[p]+","+y[p]+")",xDraw(x[p])-SIZE/2,yDraw(y[p])-SIZE/2);
			     else if (n <= 200)g.drawString(p+"",xDraw(x[p])-SIZE/2,yDraw(y[p])-SIZE/2);
				 g.drawOval (xDraw(x[p])-SIZE/2,yDraw(y[p])-SIZE/2,SIZE,SIZE);
				 g.fillOval (xDraw(x[p])-SIZE/2,yDraw(y[p])-SIZE/2,SIZE,SIZE);
	     }

		 Graph() {
			 setPreferredSize(new Dimension(size+2*margin+10,size+2*margin+10));
		 }

		 int  xDraw(int xValue){return (int)(xValue*scale)+ margin ;}
		 int  yDraw(int yValue){return (int)((y[d.MAX_Y]-yValue)*scale+margin);}

		 public void paintComponent(Graphics g) {
			super.paintComponent(g);
			 g.setColor(Color.black);
			 for (int i = 0; i < n; i++){
			    drawPoint(i,g);
		     }
			  g.setColor(Color.red);
			 // draw cohull
			 int x2 = x[theCoHull.get(0)], y2 = y[theCoHull.get(0)],x1,y1;
			 for (int i = 1; i < theCoHull.size(); i++){
				 y1 = y2; x1=x2;
				 x2 = x[theCoHull.get(i)];
				 y2 = y[theCoHull.get(i)];
		         g.drawLine (xDraw(x1),yDraw(y1), xDraw(x2),yDraw(y2));
			 }

			  g.drawLine (xDraw(x[theCoHull.get(theCoHull.size()-1)]),
			              yDraw(y[theCoHull.get(theCoHull.size()-1)]),
		                  xDraw(x[theCoHull.get(0)]),yDraw(y[theCoHull.get(0)]));
		  } // end paintComponent

	}  // end class Graph
}// end class DT
