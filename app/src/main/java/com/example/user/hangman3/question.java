package com.example.user.hangman3;

public class question {
	String q,a;
	Boolean used;
	int level;

    question(String question,String answer)
    {
        this.q=question;
        this.a=answer;
        this.used=false;
    }

	question(String question,String answer, int level)
	{
		this.q=question;
		this.a=answer;
        this.level=level;
		this.used=false;
	}
	question()
	{
		this.a="";
		this.q="";
        this.level=0;
		this.used=false;
	}
};

