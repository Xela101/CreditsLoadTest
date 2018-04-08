public class TPSSpammer {
	public void spam() {
		for(int i=0;i<Config.maxThreads;i++) {
			SpamThread spamThread = new SpamThread(String.format("Thread [%d]", i));
			spamThread.start();
		}
	}
}
