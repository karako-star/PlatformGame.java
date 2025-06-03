import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Panel_1 extends JPanel implements ActionListener {
	Timer timer;
    JButton bt;
	Image ima1, ima2, ima3;
	int gd_w;

    PlatformGame1_4 mf;  //<-- mainメソッドのある、「フレーム」のクラス
    //    この「フレーム」に、”画面１(このPanel_1のこと)” や ”画面２” などの「パネル」が貼られている

    public Panel_1(PlatformGame1_4 frame){
	mf = frame;   // MainFrameクラスで,  p1 = new Panel_1(this); として引数にthis(<--MainFrame)を
	// 渡している．　それを変数に入れて保持．

	bt = new JButton("ゲームスタート");
	add(bt);
	bt.addActionListener(this); //ボタン

	ImageIcon icon1 = new ImageIcon("ground01.png");
	ima1 = icon1.getImage(); //地面
	gd_w = ima1.getWidth(this);
	ImageIcon icon2 = new ImageIcon("idle.png");
	ima2 = icon2.getImage(); //キャラ
	
    }

    public void actionPerformed(ActionEvent e) {
	if (e.getSource()==bt) {
	    // ゲーム画面へ遷移
	    mf.panelChange("ゲーム画面");    //<-- MainFrameクラスにある panelChangeメソッドで画面の表示・非表示を切り替える
	    }
    }

    // 画面へ描画するプログラムはこのメソッドの中に書く
    public void paintComponent(Graphics g) {
    Dimension d = getSize();
	super.paintComponent(g);
	g.setColor(Color.blue);
	g.setFont( new Font ("Century", Font.ITALIC, 125));
	g.drawString("無限ラン", (d.width / 2) - 250, 200);

	int a = d.width / gd_w;
	int b = a*(a+10);
	for (int i = 0; i < a; i++) {
        g.drawImage(ima1, i * a, d.height - 100, this);
	}
	for (int i = b; i < 10000; i++) {
		g.drawImage(ima1, i, d.height - 100, this);
	}
	    g.drawImage(ima2, a*(a+5), d.height - 200, this);
    }
}
