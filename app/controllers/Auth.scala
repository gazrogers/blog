package controllers

import play.api._
import play.api.mvc._
import play.api.libs.ws.WS
import play.api.libs.concurrent.Akka
import play.api.libs.json.Json

object Auth extends Controller
{
	def loginscreen=Action
	{
		implicit request =>
			val loggedinuser=controllers.Article.getLoggedinuser(request)
			if(loggedinuser=="gareth@garethrogers.net")
				Redirect(routes.Article.admin)
			else
				Ok(views.html.login(loggedinuser))
	}

	def logoutscreen=Action
	{
		implicit request =>
			val loggedinuser=controllers.Article.getLoggedinuser(request)
			if(loggedinuser!="")
				Ok(views.html.login(loggedinuser))
			else
				Redirect(routes.Auth.loginscreen)
	}

	def login=Action(parse.urlFormEncoded)
	{
		request =>
			val promiseOfVerification=WS.url("https://verifier.login.persona.org/verify")
				.post(Map(
					"assertion" -> Seq(request.body("assertion").head),
					"audience" -> Seq("http://www.garethrogers.net")
				))
			Async
			{
				promiseOfVerification.map
				{
					i =>
						val json=Json.parse(i.body)
						val email=(json \ "email").asOpt[String]
						email match
						{
							case Some(str) => Ok(str).withSession("loggedinuser" -> str)
							case None => BadRequest
						}
				}
			}
	}

	def logout=Action
	{
		Ok("").withNewSession
	}
}