import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

/*
 * multiple loop not implemented.
 */


public class Main {
	public static void main(String[] args){
		new Main().main();
	}

	private void main(){
		try(BufferedInputStream stream =
				new BufferedInputStream(new FileInputStream("code.bf"))){

			int read;
			boolean eof = false;
			final int BACK_TAG = 0xa;
			BrainFcker brain = new BrainFcker();

			while(!eof){
				stream.mark(BACK_TAG);
				if( (read = stream.read()) == -1 ){
					eof = true;
					break;
				}

				try{
					brain.execute( (char)read );
				}catch(IllegalArgumentException ee){
					switch(ee.getMessage()){
						case BrainFcker.ADDRESS_ERROR:
							System.err.println("アドレスが負数になったよ？");
							break;
					}
					return;
				}
			}

			if(!eof)
				System.out.println("EOFいってないしおかしい。");
		}catch(IOException e){
			e.printStackTrace();
		}
	}


}


class BrainFcker {

	/* *** Property *** */

	private List<Byte> memory = new ArrayList<>();
	private int pointer = 0;
	private final int INTERVAL = 1024;

	private List<List<Character>> looper = new ArrayList<>();
	private int loopDepth = -1;

	public static final String ADDRESS_ERROR = "address error";


	/* *** ******** *** */

	public BrainFcker(){
		this.addMemory();
		for(int i=0; i<256; i++)
			looper.add(new ArrayList<Character>());
	}

	private void addMemory(){ /*{{{*/
		for(int i=0; i<INTERVAL; i++)
			memory.add((byte)0);
	} /*}}}*/
	public void execute(char operator) throws IllegalArgumentException { /*{{{*/
		try{
			System.out.print(operator);
			if(loopDepth > -1)
				looper.get(loopDepth).add(operator);

			switch(operator){
			case '>':
				if(++this.pointer >= memory.size()){
					--this.pointer;
					throw new ArrayIndexOutOfBoundsException();
				}
				break;
			case '<':
				if(--this.pointer < 0){
					throw new IllegalArgumentException(ADDRESS_ERROR);
				}
				break;
			case '+':
				memory.set(this.pointer,
						(byte)(memory.get(this.pointer) + 1));
				break;
			case '-':
				memory.set(this.pointer,
						(byte)(memory.get(this.pointer) - 1));
				break;
			case '[':
				++this.loopDepth;
				break;
			case ']':
				List<Character> recent = looper.get(this.loopDepth);
				recent.remove(recent.size()-1);
				Character[] loop = (Character[])recent.toArray(new Character[0]);
				--this.loopDepth;
				while(memory.get(this.pointer) != 0)
					for(char op : loop)
						this.execute(op);
				break;
			case '.':
				System.out.print(
						(char)(
							0|memory.get(this.pointer))
						);
				break;
			}
		}catch(ArrayIndexOutOfBoundsException over){
			this.addMemory();
			this.execute(operator);
		}

		//System.out.println(operator + ": " + this.pointer + "| " +
		//				(char)(0|memory.get(this.pointer)) +
		//				"(" + memory.get(this.pointer) + ")");
	} /*}}}*/
}
