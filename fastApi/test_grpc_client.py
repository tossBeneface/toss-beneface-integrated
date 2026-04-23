import grpc
import card_benefit_pb2
import card_benefit_pb2_grpc

def run():
    with grpc.insecure_channel('localhost:50051') as channel:
        stub = card_benefit_pb2_grpc.CardBenefitServiceStub(channel)
        
        print("--- Testing gRPC AnalyzeBestBenefit ---")
        request = card_benefit_pb2.BenefitAnalysisRequest(
            member_id=1,
            store_name="스타벅스",
            category="CAFE",
            amount=10000
        )
        response = stub.AnalyzeBestBenefit(request)
        print(f"Best Card: {response.best_card_name}")
        print(f"Total Benefit: {response.total_potential_benefit}")
        for option in response.all_options:
            print(f"- {option.card_name}: {option.discount_amount} ({option.benefit_type})")

        print("\n--- Testing gRPC AnalyzeBatchBenefits ---")
        batch_request = card_benefit_pb2.BatchBenefitRequest(
            member_id=1,
            stores=[
                card_benefit_pb2.StoreRequest(store_name="스타벅스", category="CAFE", amount=10000),
                card_benefit_pb2.StoreRequest(store_name="파리바게뜨", category="BAKERY", amount=5000),
                card_benefit_pb2.StoreRequest(store_name="CU", category="CONVENIENCE", amount=3000)
            ]
        )
        batch_response = stub.AnalyzeBatchBenefits(batch_request)
        print(f"Batch results received for {len(batch_response.results)} stores.")
        for res in batch_response.results:
            print(f"- {res.store_name}: {res.best_card_name} (Benefit: {res.total_potential_benefit})")

if __name__ == '__main__':
    run()
