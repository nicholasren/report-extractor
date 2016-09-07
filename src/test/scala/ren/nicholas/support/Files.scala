package ren.nicholas.support

import com.google.common.base.Charsets
import com.google.common.io.Resources

object Files {
  def asString(resourceName: String): String = Resources.toString(Resources.getResource(resourceName), Charsets.UTF_8)
}
