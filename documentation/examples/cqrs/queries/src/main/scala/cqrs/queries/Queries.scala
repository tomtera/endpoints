package cqrs.queries

import endpoints.play.server.{CirceEntities, Endpoints}
import play.api.routing.Router

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Implementation of the queries service.
  */
class Queries(service: QueriesService) extends QueriesEndpoints with Endpoints with CirceEntities {

  //#multiplexed-impl
  import endpoints.play.server.MuxHandlerAsync

  val routes: Router.Routes = routesFromEndpoints(

    query.implementedByAsync(new MuxHandlerAsync[QueryReq, QueryResp] {
      def apply[R <: QueryResp](query: QueryReq { type Response = R }): Future[R] =
        //#multiplexed-impl-essence
        query match {
          case FindById(id, t) => service.findById(id, t).map(MaybeResource)
          case FindAll         => service.findAll().map(ResourceList)
        }
        //#multiplexed-impl-essence
    })

  )
  //#multiplexed-impl

  // These aliases are probably due to a limitation of circe
  implicit private def circeDecoderReq: io.circe.Decoder[QueryReq] = QueryReq.queryDecoder
  implicit private def circeEncoderResp: io.circe.Encoder[QueryResp] = QueryResp.queryEncoder

}
