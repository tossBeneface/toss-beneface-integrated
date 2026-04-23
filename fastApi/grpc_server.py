import grpc
from concurrent import futures
import card_benefit_pb2
import card_benefit_pb2_grpc
import logging
from benefit_engine import analyze_best_benefit, analyze_batch_benefits

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class CardBenefitServicer(card_benefit_pb2_grpc.CardBenefitServiceServicer):
    def AnalyzeBestBenefit(self, request, context):
        logger.info(f"Received benefit analysis request for member: {request.member_id}, store: {request.store_name}")

        analysis = analyze_best_benefit(
            member_id=request.member_id,
            store_name=request.store_name,
            category=request.category,
            amount=request.amount,
        )
        logger.info(
            "Benefit analysis completed using %s engine. bestCard=%s",
            analysis.engine,
            analysis.best_card_name,
        )

        response = card_benefit_pb2.BenefitAnalysisResponse(
            best_card_name=analysis.best_card_name,
            total_potential_benefit=analysis.total_potential_benefit,
        )
        response.all_options.extend(
            [
                card_benefit_pb2.CardBenefitResult(
                    card_name=option.card_name,
                    discount_amount=option.discount_amount,
                    benefit_type=option.benefit_type,
                    description=option.description,
                )
                for option in analysis.all_options
            ]
        )
        return response

    def AnalyzeBatchBenefits(self, request, context):
        logger.info(f"Received batch benefit analysis request for member: {request.member_id}, storesCount: {len(request.stores)}")
        
        stores_input = [
            {"store_name": s.store_name, "category": s.category, "amount": s.amount}
            for s in request.stores
        ]
        
        results = analyze_batch_benefits(member_id=request.member_id, stores=stores_input)
        
        response_results = [
            card_benefit_pb2.StoreBenefitResult(
                store_name=res["store_name"],
                best_card_name=res["best_card_name"],
                total_potential_benefit=res["total_potential_benefit"]
            ) for res in results
        ]
        
        logger.info(f"Batch benefit analysis completed for {len(results)} stores")
        return card_benefit_pb2.BatchBenefitResponse(results=response_results)

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    card_benefit_pb2_grpc.add_CardBenefitServiceServicer_to_server(
        CardBenefitServicer(), server
    )
    server.add_insecure_port('[::]:50051')
    logger.info("gRPC server starting on port 50051...")
    server.start()
    server.wait_for_termination()

if __name__ == '__main__':
    serve()
