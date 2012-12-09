package ms;

import robocode.*;
import java.util.*;

//******************************************************************************************/
//  GoodBot:  determine, if a bot is a bastart or not
//
//  needed:   public void run()
//                   GoodBot.Reset();
//            public void onRobotDeath(RobotDeathEvent e)
//                   GoodBot.BotDeath(e.getName(), this);
//
//  example:  public void onScannedRobot(ScannedRobotEvent e)
//                   if (GoodBot.badBot(e.getName(), this))
//******************************************************************************************/


class GoodBot
{
	static private Hashtable Bots = new Hashtable();
	static private int GoodBots1 = 1;
	static private int GoodBots2 = 0;
	static private String bot;
	static public int KilledGoodBots1 = 0;
	static public int KilledGoodBots2 = 0;
	
	//******************************************************************************************/
	//  BadBot:  is it a bad bot?
	//******************************************************************************************/
	public static boolean badBot(String Name, AdvancedRobot in_this) {
		bot   = (String)Bots.get(Name);
		if (bot == null) classBot(Name, in_this);
		int others   = in_this.getOthers();
		if ( (bot.compareTo("1") == 0) && (others >= GoodBots1 - KilledGoodBots1) )
			return false;
		else if ( (bot.compareTo("2") == 0) && (others >= GoodBots1 - KilledGoodBots1 + GoodBots2 - KilledGoodBots2) )
			return false;
		else
			return true;
	}

	public static void BotDeath(String Name, AdvancedRobot in_this) {
		bot   = (String)Bots.get(Name);
		if (bot == null) classBot(Name, in_this);
		if ( bot.compareTo("1") == 0 ) KilledGoodBots1++;
		else if ( bot.compareTo("2") == 0 ) KilledGoodBots2++;
	}

	public static void Reset() {
		KilledGoodBots1 = 0;
		KilledGoodBots2 = 0;
	}

	private static void classBot(String Name, AdvancedRobot in_this) {
		int    M_GoodBots  = GoodBots1;
		String Gegnertyp   = botTyp(Name);
		String Mytyp       = botTyp(in_this.getName());
		if (Gegnertyp.compareTo(Mytyp) == 0) {
			bot = "1";
			if (GoodBots1 > M_GoodBots) in_this.out.println("I've found " + (GoodBots1 - 1) + " more " + Mytyp + "-Bot(s).");
		}
		else {
			GoodBots1 = M_GoodBots;
			String Gegnerpack  = botTyp2(Name);
			String Mypack      = botTyp2(in_this.getName());
			if (Gegnerpack.compareTo(Mypack) == 0) {
				bot = "2";
				GoodBots2++;
				in_this.out.println("I've found " + (GoodBots2) + " more " + Mypack + "-Bot(s).");
			}
			else
				bot = "0";
		}
		Bots.put(Name, new String(bot));
	}

	private static String botTyp(String BotName) {
		int k=BotName.indexOf("(");
		if (k != -1) { 
			int Nr=Integer.parseInt(BotName.substring(k+1, BotName.indexOf(")")),10);
			if (GoodBots1 < Nr)
				GoodBots1 = Nr;
			return BotName.substring(0, k-1);
		}
		else 
			return BotName;
	}

	private static String botTyp2(String BotName) {
		int k=BotName.indexOf(".");
		if (k != -1) { 
			return BotName.substring(0, k);
		}
		else 
			return BotName;
	}

}