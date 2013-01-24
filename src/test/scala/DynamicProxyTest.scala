import java.lang.reflect.{InvocationHandler, Method, Proxy}
import org.scalatest._


trait Foo {
  def foo(x: AnyRef): AnyRef
}

@DoNotDiscover
class SimpleSpec extends FunSuite with Foo {
  override def foo(x: AnyRef) = x
}

@DoNotDiscover
class SpecThatMixesInMatchers extends SimpleSpec with Matchers {
  override def foo(x: AnyRef) = x
}

class DynamicProxyTest extends FunSuite with Matchers {

  test("dynamic proxy creation of spec that does not mix in matchers") {
    newProxy[Foo](new SimpleSpec).foo("test") should be ("test")
  }

  test("dynamic proxy creation of spec that mixes in matchers") {
    newProxy[Foo](new SpecThatMixesInMatchers).foo("test") should be ("test")
  }

  private def newProxy[A](obj: A): A = {
    Proxy.newProxyInstance(obj.getClass.getClassLoader, obj.getClass.getInterfaces, new InvocationHandler {
      override def invoke(proxy: AnyRef, m: Method, args: Array[AnyRef]): AnyRef = {
        println(s"Invoking ${m.getName}")
        m.invoke(obj, args: _*)
      }
    }).asInstanceOf[A]
  }
}
