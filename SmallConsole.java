import java.io.*;
import java.util.*;
import java.lang.*;
 
public class SmallConsole {
	private StringBuilder cmd = new StringBuilder();
	private List<String> history = new ArrayList<>();
	private int histIndex=0;
	private int cursorIndex=0;

	public static void main(String[] args){
		SmallConsole sCons = new SmallConsole();
		try{
			//Handling ctrl+C with shutdown hook
			Runtime.getRuntime().addShutdownHook(new Thread(){
				public void run(){
					try{
						//Recover default sCons behaviour
						sCons.stty("echo");
						sCons.stty("sane");
					}catch(InterruptedException e){ e.printStackTrace(); 
					}catch(IOException e){ e.printStackTrace(); } 
				}
			});

			System.out.println("\u001b[31m" + "CTRL+C or press ESC key 3 times to exit." + "\u001b[0m");
			sCons.setTerminalToCBreak();
			mainLoop:
			while(true){
				if (System.in.available() != 0){
					int c = System.in.read();
					//System.out.println("Key code: " + c + ", " + (char) c);
					if(c >= 32 && c <= 126){ //ASCII printable character range
						System.out.print("\u001b[1000D"); //Move all the way left       
						if(sCons.cursorIndex >= sCons.cmd.length())
							sCons.cmd.append((char) c);
						else
							sCons.cmd.insert(sCons.cursorIndex, (char) c);
						sCons.cursorIndex++;
					}else if(c == specialKey.ESC.keyCode){
						int[] cmdSeq = new int[2];
						int i = 0;
						while(i<2){
							if (System.in.available() != 0){
								c = System.in.read();
								//System.out.println("|c= " + c + "|i= " + i);
								cmdSeq[i]=c;
								i++;
								Thread.sleep(10);
							}
						}
						escKeys key = sCons.recognizeEscKey(cmdSeq[0], cmdSeq[1]);
						switch(key){
							case UP: sCons.handleUpKey(); break;
							case DOWN: sCons.handleDownKey(); break;
							case LEFT: sCons.handleLeftKey(); break;
							case RIGHT: sCons.handleRightKey(); break;
							case EXIT: break mainLoop;
							case HOME: sCons.handleHomeKey(); continue mainLoop;
							case END: sCons.handleEndKey(); continue mainLoop;
							case DEFAULT: sCons.print("EcsapeKeyCode: 27 -> "+ Arrays.toString(cmdSeq)); 
									continue mainLoop;
						}
					}else if(c == specialKey.RETURN.keyCode){
						sCons.runCmd(sCons.cmd);                                                                                               
					}else if(c == specialKey.BACKSPACE.keyCode){
						sCons.handleBackspace();
					}else{
						sCons.print("KeyCode: "+ c);
						continue;
					}
					sCons.print();
				}
				Thread.sleep(100);
			}
		}catch(Exception e){ e.printStackTrace();
		}finally{
			try {
				//Recover default sCons behaviour
				sCons.stty("echo");
				sCons.stty("sane");
			}catch (Exception e) { System.err.println("Exception restoring tty config"); }
		}
	}

	private enum specialKey {
		ESC (27),
		RETURN (10),
		BACKSPACE (127);
		final int keyCode;

		private specialKey (int keyCode){
			this.keyCode = keyCode;
		}
	}

	private enum escKeys {LEFT, RIGHT, UP, DOWN, EXIT, HOME, END, DEFAULT};

	private escKeys recognizeEscKey (int c1, int c2){
		if(c1 == 91){
			if(c2 == 68)
				return escKeys.LEFT;
			if(c2 == 67)
				return escKeys.RIGHT;
			if(c2 == 65)
				return escKeys.UP;
			if(c2 == 66)
				return escKeys.DOWN;
			if(c2 == 72)
				return escKeys.HOME;
			if(c2 == 70)
				return escKeys.END;
		}
		
		if(c1 == 27 && c2 == 27)
			return escKeys.EXIT;
		return escKeys.DEFAULT;
	}

	private void runCmd (StringBuilder cmd){
		if(cmd.length() == 0){
			System.out.println();
			return;
		}

		System.out.println("\nRunning command: " + cmd);
		history.add(cmd.toString());
		histIndex=history.size() - 1;
		cmd.delete(0, cmd.length());
		cursorIndex=0; 
	}

	private void handleBackspace(){
		if(cmd.length() > 0 && cursorIndex > 0){
			cmd.deleteCharAt(--cursorIndex);
			this.print();
		}             
	}

	private void handleUpKey(){
		if(history.size() > 0){
			if(histIndex >= 0 && histIndex < history.size()){
				cmd = new StringBuilder(history.get(histIndex));
				this.print();
				if(histIndex > 0) histIndex--;
			}
		}
	}

	private void handleDownKey(){
		if(history.size() > 0 && histIndex < (history.size() - 1)){
			cmd = new StringBuilder(history.get(++histIndex));
			this.print();
		}
	}

	private void handleLeftKey(){
		cursorIndex = Math.max(0, cursorIndex -1);
		if(cursorIndex > 0)
			System.out.print("\u001b[" + cursorIndex + "C");
	}

	private void handleRightKey(){
		cursorIndex = Math.min(cmd.length(), cursorIndex + 1);
	}

	private void handleHomeKey(){
		cursorIndex = 0;
		System.out.print("\u001b[1000D"); //Move all the way left
	}

	private void handleEndKey(){
		cursorIndex = cmd.length();
		if(cursorIndex > 0)
			System.out.print("\u001b[" + cursorIndex + "C"); // Move to cursorIndex
	}

	private void print(){
		System.out.print("\u001b[0K"); //Clear line before printing to avoid left over characters
		System.out.print("\u001b[1000D"); //Move all the way left
		System.out.print(cmd);
		System.out.print("\u001b[1000D"); //Move all the way left
		if(cursorIndex > 0)
			System.out.print("\u001b[" + cursorIndex + "C"); // Move to cursorIndex
		System.out.flush();
	}

	private void print(String s){
		//System.out.println("print(Objec)");
		System.out.print("\u001b[0K"); //Clear line before printing to avoid left over characters
		System.out.print("\u001b[1000D"); //Move all the way left
		System.out.print(s);
		System.out.print("\u001b[1000D"); //Move all the way left
		if(cursorIndex > 0)
			System.out.print("\u001b[" + cursorIndex + "C"); // Move to cursorIndex
		System.out.flush();
	}

	private void setTerminalToCBreak() throws IOException, InterruptedException {

		// set the sCons to be character-buffered instead of line-buffered
		stty("-icanon min 1");

		// disable character echoing
		stty("-echo");
	}

	/**
	*  Execute the stty command with the specified arguments
	*  against the current active terminal.
	*/
	private String stty(final String args) throws IOException, InterruptedException {
		String cmd = "stty " + args + " < /dev/tty";
		//System.out.println("Runnign: " + cmd);
		return exec(new String[] {
			"bash",
			"-c",
			cmd
		});
	}

	/**
	*  Execute the specified command and return the output
	*  (both stdout and stderr).
	*/
	private String exec(final String[] cmd) throws IOException, InterruptedException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();

		Process p = Runtime.getRuntime().exec(cmd);
		int c;
		InputStream in = p.getInputStream();

		while ((c = in.read()) != -1) {
			bout.write(c);
		}

		in = p.getErrorStream();

		while ((c = in.read()) != -1) {
			bout.write(c);
		}

		p.waitFor();

		String result = new String(bout.toByteArray());
		return result;
	}

}
