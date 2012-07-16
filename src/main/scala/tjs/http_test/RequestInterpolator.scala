package tjs.http_test.utils

import tjs.http_test.model._

class RequestInterpolator(
  val scopes: Seq[IConfig]
) {
  val interpolator: Interpolator = new Interpolator(scopes)

  def interpolate(request: Request): InterpolationResult[Request] = {
    val newUri     = interpolator.interpolate(request.uri)
    val newCookies = append(request.cookies.map(c => interpolate(c)))
    val newHeaders = append(request.headers.map(h => interpolate(h)))

    // If there is a body, interpolate it.
    val bodyResult = request.body match {
      case Some(body) => Some(interpolator.interpolate(body))
      case None       => None
    }

    // If there is a body, get the interpolation result and any undefined values 
    // that might have been found.
    val (newBody, undefinedBodyValues) = bodyResult match {
      case Some(body) => (Some(body.result), body.undefinedValues)
      case None => (None, Seq())
    }

    val undefinedValues = 
      newUri.undefinedValues ++ 
        newCookies.undefinedValues ++ 
        newHeaders.undefinedValues ++ 
        undefinedBodyValues 
      
    InterpolationResult(
      Request(
        request.method,
        newUri.result,
        newHeaders.result,
        newCookies.result,
        newBody),
      undefinedValues)
  }
 
  def interpolate(header: Header): InterpolationResult[Header] = {
    val newValue = interpolator.interpolate(header.value)
    InterpolationResult(header.withValue(newValue.result), newValue.undefinedValues)
  }

  def interpolate(cookie: Cookie): InterpolationResult[Cookie] = {
    val newValue = interpolator.interpolate(cookie.value)
    InterpolationResult(cookie.withValue(newValue.result), newValue.undefinedValues)
  }

  private def append[T](results: Seq[InterpolationResult[T]]): InterpolationResult[Seq[T]] = {
    InterpolationResult(results.map(r => r.result), results.flatMap(r => r.undefinedValues))
  }
}
