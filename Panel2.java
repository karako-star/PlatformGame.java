import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;

public class Panel_2 extends JPanel implements ActionListener, KeyListener {
        JButton bt1;
        PlatformGame1_4 mf; //<-- mainメソッドのある、「フレーム」のクラス
                            // この「フレーム」に、”画面１(このPanel_2のこと)” や ”画面２” などの「パネル」が貼られている

	    Timer timer;
        int timerInterval = 50; // タイマーの時間間隔(ミリ秒)
        int elapsedTime = 0; // タイマーの経過時間（秒）
        int elapsedFrame = 0; // 経過したフレーム数（tinerIntervalごとに+1され、(1000/tinerInerval)ごとにリセットするとともにelapsedTimeを+1する）

        // 地面の変数
        int scrollSpeed = 3; // 画面スクロールの速さ
        Image groundImg;
        int groundW, groundH; // 地面ブロックの幅、高さ
        int terrainIndex = 0; // 生成する地形を識別するための番号
        int terrainLength = Terrain.terrain[0].length; // 地形ひとかたまりの配列の長さ
        int terrainNum = Terrain.terrain.length; // 地形パターンの総数
        int groundNum = terrainLength * 2; // 地面ブロックの数 // 地形のかたまり2つ分
        int[] groundX = new int[groundNum]; // 地面ブロックの左端のｘ座標
        int[] groundY = new int[groundNum]; // 地面ブロックの上端のｙ座標
        int groundXMax; // 表示中の地面ブロックにおけるgroundXの最大値 // 画面とともにscrollSpeedで動かす
        //$$$$$$$$$$$$
        boolean[] ground_alive = new boolean[groundNum];; // 地面がある場合true
        

        // プレイヤーの変数
        Image playerImg;
        int playerW, playerH; // プレイヤーの幅、高さ
        int playerX, playerY; // プレイヤーの中心座標
        int vX, vY; // プレイヤーの速度成分
        int playerDirection = 0; // プレイヤーの向き（-1 or 0 or 1）
        int playerSpeed = 8; // プレイヤーの動く速さ
        boolean isJump = false;
        boolean isGround = true; // 接地フラグ
        float jumpDuration = 0; // ジャンプし始めてからの時間
        float fallDuration = 0; // ジャンプ中でなく、地面から落下し始めてからの時間
        final int DEFAULT_GRAVITY = 5; // 重力の初期値
        int gravity = DEFAULT_GRAVITY; // 重力
        int jumpSpeed = 30; // ジャンプの初速度
        int maxFallSpeed = (int)(1.3 * jumpSpeed); // 落下速度の上限
        //$$$$$$$$$$$$
        boolean me_alive; // 生きている場合true


		// [敵の変数]
		Image tekiIma;
        int tekiX,tekiY; // 敵の中心座標
        int tekiW,tekiH; // 敵の大きさ
        int numteki=3;
        int count3=0; // キルカウンター
        boolean teki_alive; // 生きている場合true
        double random;
        

        // [隕石の変数]
		Image inseki;
		int inseki_x, inseki_y; // 隕石の座標
		int inseki_x_v; // 隕石のx方向の速さ
		int inseki_y_v; // 隕石のy方向の速さ
		boolean inseki_flg; // 降っている場合true
        

        // [武器の変数]　
		Image sword_ima;
		int sword_x, sword_y; // 武器の座標
		int sword_w, sword_h; // 武器の大きさ
		int count2; // とった武器の数
		boolean sword_alive; // 武器が取られたらfalse


        // [コインの変数]
		int m; // コインの数
		int flip; // コインの表裏
		int count1; // コインをとった枚数
		int[] coin_x, coin_y; // コインの座標
		boolean[] coin_alive; // コインが取られたらfalse
		Image coin_1_ima;
		Image coin_2_ima;
		int coin_w, coin_h; // コインの大きさ


    public Panel_2 (PlatformGame1_4 frame) {
	    mf = frame;   // MainFrameクラスで,  p1 = new Panel_2(this); として引数にthis(<--MainFrame)を
	                  // 渡している.それを変数に入れて保持．

	    bt1 = new JButton("スタート画面へ");
	    add(bt1);
	    bt1.addActionListener(this);

	    timer = new Timer(timerInterval, this);
        timer.start();

        // キーボード入力用の初期設定
        setFocusable(true);
        addKeyListener(this);

        setBackground(Color.black);
        initGround();
        initPlayer();
        initMeteor();
        initWeapon();
        initCoin();
		initteki();
    }

	
        // 地形を初期化するメソッド
        public void initGround() {
            groundImg = new ImageIcon("ground01.png").getImage();
            groundW = groundImg.getWidth(this);
            groundH = groundImg.getHeight(this);
            terrainIndex = 0; // 最初の地形パターンを指定
            for (int i = 0; i < groundNum; i++) { // 地面ブロックの位置の初期設定
                if (i < terrainLength) {
                    groundX[i] = groundW * Terrain.terrain[terrainIndex][i][0];
                    groundY[i] = groundH * Terrain.terrain[terrainIndex][i][1];    
                } else {
                    int n = i % terrainLength;
                    if (n == 0) {
                        terrainIndex = new Random().nextInt(terrainNum);
                        getMaxXOfGround();
                    }
                    groundX[i] = groundXMax + groundW * (Terrain.terrain[terrainIndex][n][0] + 1);
                    groundY[i] = groundH * Terrain.terrain[terrainIndex][n][1];
                }
                //$$$$$$$$$$$$
                ground_alive[i] = true;
            }
        }


        // 画面の左外に出た地面ブロックの座標を変えて、新たな地形を作るメソッド // タイマーで呼び出す
        void regenerateGround() {
            groundXMax -= scrollSpeed;
            for (int i = 0; i < groundNum; i++) {
                if (groundX[i] <= -groundW) {
                    generateGround(groundX, groundY, i);
                }
            }
        }
        // 地形を生成するメソッド
        void generateGround(int[] groundX, int[] groundY, int index) {
            int n = index % terrainLength;
            if (n == 0) {
                terrainIndex = new Random().nextInt(terrainNum); // 地形パターンをランダムで変更
                getMaxXOfGround();
            }
            groundX[index] = groundXMax + groundW * (Terrain.terrain[terrainIndex][n][0] + 1);
            groundY[index] = groundH * Terrain.terrain[terrainIndex][n][1];
            //$$$$$$$$$
            ground_alive[index] = true;
        }

        // 生成された地面ブロックにおいて、groundXの最大値を取得して更新するメソッド
        void getMaxXOfGround() {
            groundXMax = groundX[0];
            for (int j = 1; j < groundX.length; j++) {
                if (groundXMax < groundX[j]) {
                    groundXMax = groundX[j];
                }
            }
        }

        // プレイヤーを初期化するメソッド
        public void initPlayer() {
            playerImg = new ImageIcon("idle.png").getImage();
            playerW = playerImg.getWidth(this);
            playerH = playerImg.getHeight(this);
            playerX = 5 * groundW - playerW / 2;
            playerY = groundH * 5 - playerH / 2;
            vX = 0;
            vY = 0;
        }

		// 敵の初期設定
        public void initteki() {
            tekiIma =new ImageIcon("teki.png").getImage();
            tekiW=tekiIma.getWidth(this);
            tekiH=tekiIma.getHeight(this);
          
            // 敵の初期位置
            random=Math.random();
            tekiX= groundX[(int)(groundNum*random)] + groundW/2;
            tekiY= groundY[(int)(groundNum* random)] - tekiH / 2;
            teki_alive=true;
            if (ground_alive[(int) (groundNum* random)]==false)
                teki_alive=false; // 足元のブロックがないと敵も死ぬ
			for (int i = 0; i < groundNum; i++) {
                if (Math.abs(groundY[i] - tekiY) < tekiH/2)
				teki_alive = false; // 床に埋まった敵は表示しない
			}
        }

        // 隕石の初期設定
        public void initMeteor() {
            ImageIcon icon3 = new ImageIcon("comet.gif");
            inseki = icon3.getImage();
            Dimension d = getSize();
            // 最初の隕石が降る
            inseki_flg = true;
            inseki_x = d.width - (int) (d.width / 2 * Math.random()) + 100;
            inseki_y = -(int) (100 * Math.random()) + 50;
            inseki_x_v = (int) (11 * Math.random() + 20); // 20から30のランダムな速さ
            inseki_y_v = (int) (11 * Math.random() + 20); // 20から30のランダムな速さ
        }

        // 武器の初期設定
        public void initWeapon() {
			count2 = 0;
			ImageIcon icon6 = new ImageIcon("sword.png");
			sword_ima = icon6.getImage();
			sword_x = 750 + (int)(100 * Math.random() + 100);
			sword_y = (int)(150 * Math.random() + 250);
			sword_w = sword_ima.getWidth(this);
			sword_h = sword_ima.getHeight(this);
			sword_alive = true;
        }

        // コインの位置の初期設定
        public void initCoin() {
            m = 7;
            flip = 1; // 最初は表（奇数）
            count1 = 0; // コインをとった枚数
            coin_x = new int[m];
            coin_y = new int[m];
            coin_alive = new boolean[m];
            ImageIcon icon4 = new ImageIcon("coin_gold_1.png");
            coin_1_ima = icon4.getImage();
            ImageIcon icon5 = new ImageIcon("coin_gold_2.png");
            coin_2_ima = icon5.getImage();
            coin_w = coin_1_ima.getWidth(this);
            coin_h = coin_1_ima.getHeight(this);

            for (int i = 0; i < groundNum; i++) {
            for (int j = 0; j < m; j++) {
                coin_x[j] = 500 + (int) (1000 * Math.random());
                coin_y[j] = (int) (100 * Math.random() + 250);
                if(Math.abs(coin_y[j] - groundY[i]) < coin_h)
                continue; // コインが壁に埋まらない
                coin_alive[j] = true;
                }
            }
        }


        // KeyListenerに自動で呼び出されるメソッド(3つ）
		// キーがタイプされたときに呼び出される
		public void keyTyped(KeyEvent e) {
		}

		// キーが押されたときに呼び出される
		public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            switch (key) {
				// Aキーでプレイヤーが右に動く
                case KeyEvent.VK_A:
                    playerDirection = - 1;
                    break;
				// Dキーでプレイヤーが左に動く
                case KeyEvent.VK_D:
                    playerDirection = 1;
                    break;
				// SPACEキーでプレイヤーがジャンプする
                case KeyEvent.VK_SPACE:
                    if (!isJump && isGround) { // 空中ではジャンプできない
                        isJump = true;
                    }
				// Kキーでプレイヤーが攻撃する
				case KeyEvent.VK_K:
                    if (count2>0 && tekiX-playerX<playerW+tekiW/2 &&  Math.abs(tekiY-(playerY+playerH/2))<=tekiH/2){
                        teki_alive=false; // 武器を持っていて敵とplayerとの距離がのplayerの半分のとき敵が死ぬ 
                        count2--; // 武器が一つ減る
                        count3++; // キルカウンター
                    }
            }
		}

		// キーが離されたときに呼び出される
		public void keyReleased(KeyEvent e) {
            int key = e.getKeyCode();
            switch (key) {
                case KeyEvent.VK_A:
                    if (playerDirection == -1) playerDirection = 0;
                    break;
                case KeyEvent.VK_D:
                    if (playerDirection == 1) playerDirection = 0;
                    break;
                case KeyEvent.VK_SPACE:
                    // スペースキーをすぐに離すと小ジャンプに
                    if (isJump && vY < -jumpSpeed / 2) {
                        gravity *= 1.5;
                }
            }    
		}

		// 敵の動き
		void moveTeki() {
            tekiX -= scrollSpeed;
            if (teki_alive) {
                // 倒されなかった敵の復活
                if (tekiX < -50) {
                tekiX= groundX[(int)(groundNum*random)]+groundW/2;
                tekiY= groundY[(int) (groundNum* random)] - tekiH / 2;
                }
                // 敵に殺される
                if (Math.abs(playerX - tekiX)<=playerW/2 &&Math.abs(playerY-tekiY)<playerH/2) {
                    gameOver();
                }
            } else {
                // 倒された敵の復活
                if (tekiX < -50) {
                    teki_alive = true;
                    tekiX= groundX[(int)(groundNum*random)]+groundW/2;
                    tekiY= groundY[(int) (groundNum* random)] - tekiH / 2;
                } 
			} 
		} 

    public void actionPerformed(ActionEvent e) {
	if (e.getSource()==bt1) { // "スタート画面へ"というボタンが押されたら
	    // 画面１へ遷移
	    mf.panelChange("スタート画面"); //<-- MainFrameクラスにある panelChangeメソッドで画面の表示・非表示を切り替える
	}

	if (e.getSource() == timer) {
		Dimension d = getSize();
		// 地面を動かす
		for (int i = 0; i < groundNum; i++) {
			groundX[i] -= scrollSpeed;
		}

		// プレイヤーの速度成分を決める
		int nextX, nextY; // 仮の移動先座標
		vX = playerDirection * playerSpeed - scrollSpeed;
		nextX = playerX + vX; // 仮の移動先を計算
		
		if (isJump) {
			jumpDuration += 0.5f;
			vY = (int)(gravity * jumpDuration - jumpSpeed); // 速度のｙ成分を計算
			nextY = playerY + vY; // 仮の移動先を計算
			boolean[] collision = collisionDetection(nextX, nextY); // 地面ブロックとの当たり判定
			if (collision[0]) { // 上
				jumpDuration = 0;
				gravity = DEFAULT_GRAVITY;
				isJump = false;
			}
			if (collision[1]) { // 下
				jumpDuration = 0;
				gravity = DEFAULT_GRAVITY;
				isJump = false;
			}
			if (collision[2]) { // 右
				
			}
			if (collision[3]) { // 左
				
			}
		} else if (!isGround) { // 落下時（ジャンプ中でない）
			fallDuration += 0.5f;
			vY = (int)(gravity * fallDuration); // 速度のｙ成分を計算(自由落下)
			nextY = playerY + vY; // 仮の移動先を計算
			boolean[] collision = collisionDetection(nextX, nextY); // 地面ブロックとの当たり判定
			if (collision[0]) { // 上
				
			}
			if (collision[1]) { // 下
				isGround = true; // 1つでも地面ブロックに接地していたら接地フラグをtrueにする
				fallDuration = 0; // 落下時間をリセット
			}
			if (collision[2]) { // 右
				
			}
			if (collision[3]) { // 左
				
			}
		} else { // 接地しているとき
			nextY = playerY + vY; // 仮の移動先を計算
			isGround = false; // 接地フラグを一度falseにする
			boolean[] collision = collisionDetection(nextX, nextY); // 地面ブロックとの当たり判定
			if (collision[0]) { // 上
				
			}
			if (collision[1]) { // 下
				isGround = true; // 1つでも地面ブロックに接地していたら接地フラグをtrueにする
			}
			if (collision[2]) { // 右
				
			}
			if (collision[3]) { // 左
				
			}
		}
		
		nextX = playerX + vX; // 仮の移動先を再度計算
		if (nextX > d.width - playerW / 2) vX = 0; // 画面より先（右）には行けないようにする
		if (vY > maxFallSpeed) vY = maxFallSpeed; // 落下速度を制限
		// プレイヤーを動かす
		playerX += vX;
		playerY += vY;
		// プレイヤーが画面より下に落ちる、または、左外に出るとゲームオーバー（ただし、ゲーム開始後3秒以降）
		if ((playerY > d.height || playerX < -50) && elapsedTime > 3) {
			gameOver();
		}

		// 地形を更新
		regenerateGround();

		// 経過時間を記録
		elapsedFrame++;
		if (elapsedFrame >= 1000/timerInterval) {
			elapsedTime++;
			elapsedFrame = 0;
		}

		//$$$$$$$$$$$$$$$
		// コインを動かす
		flip++; // コインがキラキラ回転する
		for (int j = 0; j < m; j++) {
			coin_x[j] -= scrollSpeed;
			if(coin_alive[j]) {
				// とられなかったコインの復活
				if(coin_x[j] < -50) {
				coin_x[j] = d.width + (int)(100 * Math.random() + 100);
				coin_y[j] = (int)(100 * Math.random() + 250);
				}
				// コインをとる
				if(Math.abs(coin_x[j] - playerX) < coin_w && Math.abs(coin_y[j] - playerY) < coin_h) {
					coin_alive[j] = false;
					count1++;
				}
			} else {
				// とられたコインの復活
				if(coin_x[j] < -50) {
					coin_alive[j] = true;
					coin_x[j] = d.width + (int)(100 * Math.random() + 100);
					coin_y[j] = (int)(100 * Math.random() + 250);
				}
			}
		}

		// 隕石を動かす
		if (inseki_flg == true) {
			inseki_y += inseki_x_v;
			inseki_x -= inseki_y_v;

			// 隕石の当たり判定
			for (int i = 0; i < groundNum; i++) {
				if (ground_alive[i] &&
						Math.abs(groundX[i] + groundW/2 - inseki_x) < groundW &&
						groundY[i] < inseki_y && groundY[i] + groundH > inseki_y) {
					ground_alive[i] = false; // 床が壊れる
					groundY[i] = 2 * d.height; // 壊れた地面ブロックを画面外に移動する
					inseki_flg = false; // 隕石が消える
				}
			}
			// 隕石が人に当たったらゲームオーバー
			if (Math.abs(playerX - inseki_x) < playerW / 2 && Math.abs(playerY - inseki_y) < playerH / 2) {
				gameOver();
			}

			// 隕石が敵に当たると敵が死ぬ
			if (teki_alive && Math.abs(tekiX - inseki_x) < tekiW / 2 && Math.abs(tekiY - inseki_y) < tekiH / 2) {
				teki_alive = false;
			}

			// 隕石が画面下に消えたら状態をfalseに
			if (inseki_y > d.height) {
				inseki_flg = false;
			}
		} else { // 隕石が降っていないとき
			if (Math.random() < 0.01) { // 後続の隕石は1%の確率で降る
				inseki_flg = true;
				inseki_x = d.width - (int) (100 * Math.random()) + 50;
				inseki_y = -(int) (100 * Math.random()) + 50;
			}
		}

		// 武器を動かす
		sword_x -= scrollSpeed;
		if (sword_alive == true) {
			// とられなかった武器の復活
			if (sword_x < -50) {
				sword_x = 750 + (int) (50 * Math.random() + 100);
				sword_y = (int) (150 * Math.random() + 250);
			}
			if (Math.abs(sword_x - playerX) < sword_w && Math.abs(sword_y - playerY) < sword_h) {
				sword_alive = false; // 武器が取られる
				count2++;
			}
		} else {
			//　とられた武器の復活
			if (sword_x < -50) {
				sword_alive = true;
				sword_x = d.width + (int) (50 * Math.random() + 100);
				sword_y = (int) (100 * Math.random() + 250);
			}
		}
	}
	    moveTeki();
		repaint();
    }

	// ゲームオーバー時の処理
	public void gameOver() {
		System.out.println("GAME OVER...");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.exit(0);
	}

	// 当たり判定メソッド
	// 判定結果をboolean型配列{上, 下, 右, 左}として返す
	public boolean[] collisionDetection(int nextX, int nextY) {
		boolean[] result = { false, false, false, false }; // 判定結果の配列{上, 下, 右, 左}
		for (int i = 0; i < groundNum; i++) {
			// プレイヤーの上端と地面ブロックとの当たり判定 // 最初の条件につけた"="がないとすり抜ける
			if (nextX >= groundX[i] && nextX < (groundX[i] + groundW)
					&& (nextY - playerH / 2) < (groundY[i] + groundH)
					&& (nextY + playerH / 2) > (groundY[i] + groundH)
					&& vY <= 0) {
				playerY = groundY[i] + groundH + playerH / 2;
				vY = 0;
				// System.out.println("TOP");
				result[0] = true;
			}
			// プレイヤーの下端と地面ブロックとの当たり判定 // 最初の条件につけた"="がないとすり抜ける
			else if (nextX >= groundX[i] && nextX < (groundX[i] + groundW) && (nextY - playerH / 2) < groundY[i]
					&& (nextY + playerH / 2) >= groundY[i] && vY >= 0) {
				playerY = groundY[i] - playerH / 2;
				vY = 0;
				// System.out.println("BOTTOM");
				result[1] = true;
			}
			// 左右の当たり判定は、必要なブロックについてのみ行う
			else {
				if (i == 0 || groundY[i - 1] != groundY[i] || Math.abs(groundX[i] - groundX[i - 1]) > groundW) {
					// プレイヤーの右端と地面ブロックとの当たり判定
					if ((nextX - playerW / 2) < groundX[i] && (nextX + playerW / 2) > groundX[i] &&
							nextY >= groundY[i] && nextY < (groundY[i] + groundH) && vX >= 0) {
						playerX = groundX[i] - playerW / 2;
						vX = 0;
						// System.out.println("RIGHT");
						result[2] = true;
					}
				}
				if (i == (groundNum - 1) || groundY[i] != groundY[i + 1]
						|| Math.abs(groundX[i] - groundX[i + 1]) > groundW) {
					// プレイヤーの左端と地面ブロックとの当たり判定
					if ((nextX - playerW / 2) < (groundX[i] + groundW)
							&& (nextX + playerW / 2) > (groundX[i] + groundW)
							&&
							nextY >= groundY[i] && nextY < (groundY[i] + groundH) && vX <= 0) {
						playerX = groundX[i] + groundW + playerW / 2;
						vX = 0;
						// System.out.println("LEFT");
						result[3] = true;
					}
				}
			}
		}
		return result;
	}

    // 画面へ描画するプログラムはこのメソッドの中に書く
    public void paintComponent(Graphics g) {
	super.paintComponent(g);
	// 地面を描画
	for (int i = 0; i < groundNum; i++) {
		if (ground_alive[i]) // 地面が壊れていない場合
		g.drawImage(groundImg, groundX[i], groundY[i], this);
	}

	// プレイヤーを描画
	g.drawImage(playerImg, playerX - playerW / 2, playerY - playerH / 2, this);

	//敵を描画
	for(int k=0; k<numteki; k++) {
		if(teki_alive) // 敵が生きている場合
	    g.drawImage(tekiIma,tekiX-tekiW/2,tekiY-tekiH/2,this);
	}

	//$$$$$$$$$$$$$$
	// コインの描画
	for (int j = 0; j < m; j++) {
		if (coin_alive[j] && flip % 2 == 1)
			g.drawImage(coin_1_ima, coin_x[j], coin_y[j], this);
		if (coin_alive[j] && flip % 2 == 0)
			g.drawImage(coin_2_ima, coin_x[j], coin_y[j], this);
	}

	// コインの枚数の表示
	g.setColor(Color.white);
	g.drawString("コイン", 50, 25);
	g.drawString(String.valueOf(count1), 150, 25);

	// 隕石の描画
	if (inseki_flg) 
	g.drawImage(inseki, inseki_x, inseki_y, this);

	// 武器の描画
	if (sword_alive)
	g.drawImage(sword_ima, sword_x, sword_y, this);

	// 武器の数の表示
	g.setColor(Color.white);
	g.drawString("攻撃可能回数", 50, 50);
	g.drawString(String.valueOf(count2), 150, 50);

	//　倒した敵の数の表示
	g.setColor(Color.white);
	g.drawString("倒した敵", 50, 75);
	g.drawString(String.valueOf(count3), 150, 75);
    }
}
