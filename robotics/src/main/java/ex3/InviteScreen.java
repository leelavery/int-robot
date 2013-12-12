package ex3;

import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ex3.Speech.RecognitionResult;
import ex3.Speech.SpeechListener;

@SuppressWarnings("serial")
public class InviteScreen extends JFrame implements ActionListener, SpeechListener {

	private boolean answer, answered;
	private int timeInSecs = 15;
	private JTextField timeleft;
	private Speech speech;
	private int timeLeft;
	
	public InviteScreen(Speech speech)  {
		this.speech = speech;
		speech.addListener(this);
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(4, 1));
		panel.add(new JTextField("I am inviting you to my meeting"));

		JButton accept = new JButton("Ok, Cool");
		accept.addActionListener(this);
		accept.setActionCommand("true");

		JButton reject = new JButton("Hell No!");
		reject.addActionListener(this);
		reject.setActionCommand("false");

		timeleft = new JTextField("Defaulting to no in: " + timeInSecs);
		
		panel.add(accept);
		panel.add(reject);
		panel.add(timeleft);
		add(panel);

		setSize(400, 400);
		// Make window the whole screen
		setSize(Toolkit.getDefaultToolkit().getScreenSize());

		
	}
	
	public boolean invite() {	
		answered = false;
		answer = false;
		speech.startRecognising();
		setVisible(true);

		// Start Timer
		setTimeout(timeInSecs);
		while (timeLeft > 0 && !answered) {
			try {
				timeleft.setText("Defaulting to no in: " + timeLeft);
				timeLeft--;
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		speech.stopRecognising();
		setVisible(false);
		
		return answer;
	}
	
	private void setTimeout(int secs) {
		timeLeft = secs;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		answer = Boolean.valueOf(e.getActionCommand().toLowerCase());
		answered = true;
	}
	
	@Override
	public void speechHeard(RecognitionResult data) {
		System.out.println("heard: " + data.getText());
		setTimeout(timeInSecs);
		String text = data.getText().toLowerCase();
		System.out.println("heard: " + text);
		if (text.contains("yes")) {
			answer = true;
			answered = true;
		} else if (text.contains("no")) {
			answer = false;
			answered = true;
		} else {
			speech.play("didnt-hear", true);
			System.out.println("heard something other than yes or no..");
		}
	}
	
	public static void main(String[] args) {
		InviteScreen invite = new InviteScreen(new Speech());
		
		while(true) {
			System.out.println("Answer: " + invite.invite());
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}