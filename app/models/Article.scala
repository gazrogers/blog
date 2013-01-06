package models

import play.api.Play.current
import play.api.db.DB
import anorm.SQL
import anorm.SqlQuery
import anorm.RowParser
import anorm.ResultSetParser
import util.matching.Regex
import java.util.Date
import java.text.DateFormat

case class Article(id: Int, title: String, date: Date, article: String)

object Article
{
	val sql: SqlQuery=SQL("select * from articles order by id desc")
	val articleParser: RowParser[Article]=
	{
		import anorm._
		import anorm.SqlParser._
		int("id") ~
		str("title") ~
		date("date") ~
		str("article") map
		{
			case id ~ title ~ date ~ article => Article(id, title, date, article)
		}
	}
	val articlesParser: ResultSetParser[List[Article]]=
	{
		articleParser *
	}

	def findArticleById(id: Int):Option[Article]=DB.withConnection {
		implicit connection => sql.as(articlesParser).find(_.id==id)
	}
	def getIndex: List[Article]=DB.withConnection {
		implicit connection => sql.as(articlesParser)
	}
	def getIndex(pagenum: Int, perpage: Int): List[Article]=DB.withConnection {
		implicit connection => sql.as(articlesParser).drop((pagenum - 1) * perpage).take(perpage)
	}
	def addarticle(title: String, article: String)=
	{
		DB.withConnection
		{
			implicit connection =>
				SQL("insert into articles(title,date,article) values({title},NOW(),{article})")
				.on("title" -> title, "article" -> article)
				.executeUpdate()
		}
	}
	def edit(id: Int, title: String, article: String)=DB.withConnection {
		implicit connection =>
			SQL("update articles set title={title}, article={article} where id={id}")
			.on("title" -> title, "article" -> article, "id" -> id)
			.executeUpdate()
	}
	def delete(id: Int)=DB.withConnection {
		implicit connection => SQL("delete from articles where id={id}").on("id" -> id).executeUpdate()
	}

	def parseMarkdownRedux(text: String)=
	{
		//TODO - replace this with a decent Markdown parser - get some practice with parser combinators in the process

		val blockquote="""^>""".r
		val boldtext="""(.*?)\*\*(\w+)\*\*(.*?)""".r
		val italictext="""(.*?)\*(\w+)\*(.*?)""".r
		val codetext="""(.*?)`(\w+)`(.*?)""".r
		val linktext="""(.*?)\[(.*?)\]\((.*?)\)(.*?)""".r
		val linebreaks="""(.*?)  $""".r
		val codelines="""(    |\t)(.*)""".r
		val listlines="""^ {0,3}(-|\+|\*) """.r
		val orderedlistlines="""^ {0,3}\d+\. """.r

		val header1="""(?s)([ \t]*)([^\n]*?)\n[ \t]*=+($|\n)""".r
		val header2="""(?s)([ \t]*)([^\n]*?)\n[ \t]*-+($|\n)""".r
		val codeblock="""(?s)(?<=\n\n)(#codeblock#(.*?)($|\n))+""".r
		val stripcodeblock="""#codeblock#""".r
		val listblock="""(?s)(?<=\n)\n(<li>.*?($|\n))+""".r
		val orderedlistblock="""(?s)(?<=\n)\n(#ol#<li>.*?($|\n))+""".r
		val stripolist="""#ol#""".r
		val quoteblock="""(?s)(\n#blockquote#([^\n]*))+""".r
		val stripquote="""#blockquote#""".r
		val para="""(^|\n\n)(?!<)""".r

		def lines=text.filter(_!='\r').split('\n')

		val afterinline=lines.map
		{
			line =>
				val afterblockquote=blockquote replaceAllIn(line, "#blockquote#")
				val afterbold=boldtext replaceAllIn(afterblockquote, "$1<strong>$2</strong>$3")
				val afterital=italictext replaceAllIn(afterbold, "$1<em>$2</em>$3")
				val aftercode=codetext replaceAllIn(afterital, "$1<code>$2</code>$3")
				val afterlink=linktext replaceAllIn(aftercode, "$1<a href=\"$3\">$2</a>$4")
				val afterlinebreaks=linebreaks replaceAllIn(afterlink, "$1<br/>")
				val aftercodelines=codelines replaceAllIn(afterlinebreaks, "#codeblock#$2")
				val afterlistlines=listlines replaceAllIn(aftercodelines, "<li>")
				orderedlistlines replaceAllIn(afterlistlines, "#ol#<li>")
		}.mkString("\n")
		val afterheader1=header1 replaceAllIn(afterinline, "$1<h3>$2</h3>\n")
		val afterheader2=header2 replaceAllIn(afterheader1, "$1<h4>$2</h4>\n")
		val aftercodeblock=stripcodeblock replaceAllIn(codeblock replaceAllIn(afterheader2, "\n<pre><code>$0</code></pre>\n"), "")
		val afterlistblock=listblock replaceAllIn(aftercodeblock, "\n<ul>$0</ul>\n")
		val afterorderedlistblock=orderedlistblock replaceAllIn(afterlistblock, "\n<ol>$0</ol>\n")
		val afterstripolist=stripolist replaceAllIn(afterorderedlistblock, "")
		val afterquoteblock=quoteblock replaceAllIn(afterstripolist, "\n<blockquote><p>$0</blockquote>\n")
		val afterstripquote=stripquote replaceAllIn(afterquoteblock, "")
		para replaceAllIn(afterstripquote, "\n<p>")
	}
}