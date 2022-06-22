//Abstract Builder
class abstract class TextConverter{
	abstract void convertCharacter(char c);
	abstract void convertParagraph();
}

// Product
class ASCIIText{
	public void append(char c){ //Implement the code here }
}

//Concrete Builder
class ASCIIConverter extends TextConverter{
	ASCIIText asciiTextObj;//resulting product

	/*converts a character to target representation and appends to the resulting*/
	object void convertCharacter(char c){
		char asciiChar = new Character(c).charValue();
			//gets the ascii character
		asciiTextObj.append(asciiChar);
	}
	void convertParagraph(){}
	ASCIIText getResult(){
		return asciiTextObj;
	}
}

//This class abstracts the document object
class Document{
	static int value;
	char token;
	public char getNextToken(){
		//Get the next token
		return token;
	}
}

//Director
class RTFReader{
	private static final char EOF='0'; //Delimitor for End of File
	final char CHAR='c';
	final char PARA='p';
	char t;
	TextConverter builder;
	RTFReader(TextConverter obj){
		builder=obj;
	}
	void parseRTF(Document doc){
		while ((t=doc.getNextToken())!= EOF){
			switch (t){
				case CHAR: builder.convertCharacter(t);
				case PARA: builder.convertParagraph();
			}
		}
	}
}

//Client
public class Client{
	void createASCIIText(Document doc){
		ASCIIConverter asciiBuilder = new ASCIIConverter();
		RTFReader rtfReader = new RTFReader(asciiBuilder);
		rtfReader.parseRTF(doc);
		ASCIIText asciiText = asciiBuilder.getResult();
	}
	public static void main(String args[]){
		Client client=new Client();
		Document doc=new Document();
		client.createASCIIText(doc);
		system.out.println("This is an example of Builder Pattern");
	}
}
