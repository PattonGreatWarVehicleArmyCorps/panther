package ak;
import java.util.*;
import robocode.*;
import java.io.*;
/**
 * DataWriter - a class by Arun Kishore
 */
public class DataWriter extends Thread
{
	Fermat robot;
	Vector stats;
	
	DataWriter(Fermat robot)
	{
		this.robot = robot;
		stats = new Vector();
		for(int i =0;i<robot.otherBots.size();i++)
		{
			OtherBot bot = (OtherBot)robot.otherBots.elementAt(i);
			for(int j=0;j<bot.stats.size();j++)
			{
				Statistic botStat = (Statistic)bot.stats.elementAt(j);
				Statistic stat = new Statistic();
				stat.shots = botStat.shots;
				stat.hits = botStat.hits;
				stats.add(stat);
			}
		}
	}
	
	public void run()
	{
		try
		{
			int k=0;
			for(int i=0;i<robot.otherBots.size();i++)
			{
				OtherBot bot = (OtherBot)robot.otherBots.elementAt(i);
				PrintStream tps = new PrintStream(new RobocodeFileOutputStream(robot.getDataFile(bot.name+".txt")));
				for(int j=0;j<bot.stats.size();j++)
				{
					Statistic stat = (Statistic)stats.elementAt(k++);
					tps.println(stat.shots);
					tps.println(stat.hits);
				}
				if(tps.checkError())
					robot.out.println("Error writing data to robot file");
				tps.close();
			}
		}
		catch(Exception ex)
		{
			robot.out.println("Could not write Data");
		}
	}
}
