package osaka.senbatsu;
import robocode.*;
//import java.awt.Color;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * TestOne - a robot by (your name here)
 */
public class TestOne extends Robot
{
	private boolean searchFirst = true;
	private boolean findEnemy; //findEnemy 敵がみつかったらtrueになる
	private double enemyAngle;
	private double kaku;
	private double distance;
	private double teki_X;
	private double teki_Y;
	private double teki_kaku; 
	private double teki_heding;
	private double teki_heding2 = 0;
	private double velocity;
	double p = 1;
	double n = 1;
	double tn = 0;


	public void run(){
//		setColors(Color.yellow, Color.yellow, Color.red);
	setAdjustRadarForGunTurn(true); //大砲が回転するときに、レーダーが自動的に反対方向に回転するように設定
	setAdjustGunForRobotTurn(true); //ロボットが回転するときに、大砲が自動的に反対方向に回転するように設定
	//つまり　まず，「ボディ」，「砲塔」，「レーダ」が別々に動くようにする．
	while(true){
	ahead(100);
	setTurnRight(60);	
	turnRadarRight(360); //レーダーを右に一回転
	//ある条件のもとで　レーダーを右左に一回ずつ動かす
	do { // getRadarHeading()レーダーの向きの絶対角度
	findEnemy = false; //enemyAngle:敵までのの絶対角度
	double la = getRelAngle(enemyAngle+20, getRadarHeading());// getRelAngl 下で定義されたメッソド
	turnRadarRight(la);
	double ra = getRelAngle(enemyAngle-20, getRadarHeading());
	turnRadarRight(ra);
	} while(findEnemy);
	}
	}

	// レーダーで敵がみつかったら
	public void onScannedRobot(ScannedRobotEvent e){
	findEnemy = true;	//敵をみつけたかどうかのフラッグ
	// turnRadarRight(0);	//右に０回転...何もしないことと同じなのになぜ必要なのか

	myscan(e);
	mypattan1();
	myfire(e);

//		enemyAngle = e.getBearing() + getHeading();	 //敵までのの絶対角度


	/* scan()
	onScannedRobot イベントに割り込む
	onScannedRobot の処理中に scan() を呼び出して
	、引き続きロボットが見えている場合
	、システムは処理中の onScannedRobot イベントにただちに割り込んで、最初からこれをやり直します。
	この呼び出しはただちに実行されます。スキャンによってロボットが見つかった場合、
	onScannedRobot(robocode.ScannedRobotEvent) が発生します。 
	*/	
	scan();
	}

	//壁にぶつかったら うまく作動しない
	public void onHitWall(HitWallEvent e) {
	tn = getRelAngle(45, getHeading());
	p=-1;
	setAhead(20*p);
	setTurnRight(tn);

	if (getX()<50) p=-1;
	}





	//絶対角度 hdから gdへの（右回りの）相対角度を返す．返す角度は -180～180
	//つまり　gdへ回転さしたいけれど　hdを基準にして最短でその回転位置へもっていきたいわけか
	//右へ355度まわすより左に5度まわしたほうが　早い
	public double getRelAngle(double gd, double hd){
	double kd = gd - hd;
	if (kd<-180)	 return kd + 360;
	else if (kd>180) return kd - 360;
	return kd;
	}


	public void myscan(ScannedRobotEvent mye){
	//発見した敵ロボットの現在位置
	kaku = Math.toRadians(mye.getBearing()+getHeading());//敵までの絶対角度を得る 多分ラジアン形式
	distance = mye.getDistance();	 //発見したロボットまでの距離を調べます
	teki_X = distance * Math.sin(kaku);	 //kaku=0度　で　通常の　90度だからこうなる　
	teki_Y = distance * Math.cos(kaku);

	//自ロボットの進行方向を基準として、発見したロボットのいる方向を得る
	teki_kaku = mye.getBearing();

	//敵ロボットの進行方向を得る
	teki_heding = mye.getHeading();

	//敵ロボットの速度を得る
	velocity = mye.getVelocity();



	}	

	// 敵の周りを回るような感じで動くように　
	// getRelAngleをつかわないと　なぜかぐるぐる回りをする
	// 70にしておく　90だと...
	public void mypattan1(){

//		if (n%4==0){
	tn = getRelAngle(enemyAngle-70, getHeading());

	setAhead(20*p);
	setTurnRight(tn);
	// setTurnRight(1800);
//		 if ( (getX()<50) | (getY()<50) | (getX()>getBattleFieldWidth()-70) | (getY()>getBattleFieldHeight()-70) ){
//		 p=-1;
//		 }

//		}
//		n = n+1;
	}

	public void myfire(ScannedRobotEvent mye){
	enemyAngle = mye.getBearing() + getHeading();	 //敵までのの絶対角度

	//敵までの絶対角度　と　砲台の絶対角度　から-180～180度で砲台を動かして標準をあわせる	
	double kd = getRelAngle(enemyAngle, getGunHeading());	//getGunHeading())：砲台の絶対角度
	turnGunRight(kd);	//多分kdがマイナスなら左回り？
	if (getGunHeat()==0){	//この値が0でないと弾丸を発射できない　発射メソッドは成功するが実際の発射は　これが0になるまで
	fire(3);	//パワー3の弾丸発射
	}

	}
	
	


/**
 * new function
 */
public double getRelativeHeadingRadians() {
    double relativeHeading = getHeading();
    if (kaku < 1) {
        relativeHeading =
                normalizeAbsoluteAngleRadians(relativeHeading + Math.PI);
    }
    return relativeHeading;
}

public void reverseDirection() {
    double distance = (getRelativeHeadingRadians() * kaku);
    kaku *= -1;
    setAhead(distance);
}

public void setAhead(double distance) {
    double relativeDistance = (distance * kaku);
    ahead(relativeDistance);
    if (distance < 0) {
    	kaku *= -1;
    }
}

public void setBack(double distance) {
    double relativeDistance = (distance * kaku);
    back(relativeDistance);
    if (distance > 0) {
    	kaku *= -1;
    }
}

public void setTurnLeft(double angle) {
    double turn = normalizeRelativeAngleRadians(angle);
    if (Math.abs(turn) > HALF_PI) {
        reverseDirection();
        if (turn < 0) {
            turn = (HALF_PI + (turn % HALF_PI));
        } else if (turn > 0) {
            turn = -(HALF_PI - (turn % HALF_PI));
        }
    }
    setTurnLeft(turn);
}

public void setTurnRight(double angle) {
    double turn = normalizeRelativeAngleRadians(angle);
    if (Math.abs(turn) > HALF_PI) {
        reverseDirection();
        if (turn < 0) {
            turn = (HALF_PI + (turn % HALF_PI));
        } else if (turn > 0) {
            turn = -(HALF_PI - (turn % HALF_PI));
        }
    }
    setTurnRight(turn);
}












/**
 * option
 */

private static final double DOUBLE_PI = (Math.PI * 2);
private static final double HALF_PI = (Math.PI / 2);

public double calculateBearingToXYRadians(double sourceX, double sourceY,
    double sourceHeading, double targetX, double targetY) {
        return normalizeRelativeAngleRadians(
           Math.atan2((targetX - sourceX), (targetY - sourceY)) -
               sourceHeading);
    }

public double normalizeAbsoluteAngleRadians(double angle) {
   if (angle < 0) {
        return (DOUBLE_PI + (angle % DOUBLE_PI));
    } else {
        return (angle % DOUBLE_PI);
    }
}

public static double normalizeRelativeAngleRadians(double angle) {
    double trimmedAngle = (angle % DOUBLE_PI);
    if (trimmedAngle > Math.PI) {
        return -(Math.PI - (trimmedAngle % Math.PI));
    } else if (trimmedAngle < -Math.PI) {
        return (Math.PI + (trimmedAngle % Math.PI));
    } else {
        return trimmedAngle;
    }
}
}
								