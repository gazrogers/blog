package controllers

import play.api._
import play.api.mvc._
import java.util.Date

object Article extends Controller
{
	def index=Action
	{
		implicit request =>
			val loggedinuser=getLoggedinuser(request)
			val articles=models.Article.getIndex(1, 5)
			Ok(views.html.index(articles, loggedinuser))
	}

	def article(id: Int)=Action
	{
		implicit request =>
			val loggedinuser=getLoggedinuser(request)
			val thisarticle=models.Article.findArticleById(id)
			thisarticle match
			{
				case Some(a) => Ok(views.html.article(a, loggedinuser))
				case None => NotFound("Post not found")
			}
	}

	def editor=Action
	{
		implicit request =>
			val loggedinuser=getLoggedinuser(request)
			if(loggedinuser=="gareth@garethrogers.net")
				Ok(views.html.editor(models.Article(0,"",new Date,""), loggedinuser))
			else
				Redirect(routes.Auth.loginscreen)
	}

	def delete(id: Int)=Action
	{
		implicit request =>
			val loggedinuser=getLoggedinuser(request)
			if(loggedinuser=="gareth@garethrogers.net")
			{
				models.Article.delete(id)
				Redirect(routes.Article.admin)
			}
			else
				Redirect(routes.Auth.loginscreen)
	}

	def edit(id: Int)=Action
	{
		implicit request =>
			val loggedinuser=getLoggedinuser(request)
			if(loggedinuser=="gareth@garethrogers.net")
			{
				val thisarticle=models.Article.findArticleById(id)
				thisarticle match
				{
					case Some(a) => Ok(views.html.editor(a, loggedinuser))
					case None => NotFound("Post not found")
				}
			}
			else
				Redirect(routes.Auth.loginscreen)
	}

	def newarticle=Action(parse.urlFormEncoded)
	{
		implicit request =>
			if(request.body("postid").head.toInt==0)
				models.Article.addarticle(request.body("title").head, request.body("article").head)
			else
				models.Article.edit(request.body("postid").head.toInt, request.body("title").head, request.body("article").head)
			Redirect(routes.Article.index)
	}

	def admin=Action
	{
		implicit request =>
			val loggedinuser=getLoggedinuser(request)
			if(loggedinuser=="gareth@garethrogers.net")
			{
				val articles=models.Article.getIndex
				Ok(views.html.admin(articles, loggedinuser))
			}
			else
				Redirect(routes.Auth.loginscreen)
	}

	def format=Action(parse.urlFormEncoded)
	{
		implicit request =>
			Ok(models.Article.parseMarkdownRedux(request.body("string").head))
	}

	def getLoggedinuser(request: Request[AnyContent])=request.session.get("loggedinuser").getOrElse("")
}