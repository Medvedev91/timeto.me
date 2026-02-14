import SwiftUI
import shared

private let bottomMargin: CGFloat = (HomeScreen__itemHeight - HomeScreen__itemCircleHeight) / 2
private let shape = RoundedRectangle(cornerRadius: 14, style: .continuous)

struct HomeRateView: View {
    
    let homeVm: HomeVm
    let homeState: HomeVm.State
    
    var body: some View {
        VStack {
            
            Text(homeState.rateLine1)
                .textAlign(.leading)
                .fontWeight(.medium)
                .padding(.horizontal, 12)
            
            Text(homeState.rateLine2)
                .textAlign(.leading)
                .fontWeight(.medium)
                .padding(.horizontal, 12)
                .padding(.top, 5)
            
            HStack {
                
                Button(
                    action: {
                        homeVm.onRateStart()
                        openAppStoreReviewPage()
                    },
                    label: {
                        HStack {
                            // The same text as App Store title sheet
                            Text("Write a Review on App Store")
                                .font(
                                    .system(
                                        size: HomeScreen__itemCircleFontSize,
                                        weight: HomeScreen__itemCircleFontWeight
                                    )
                                )
                                .foregroundColor(.black.opacity(0.8))
                                .minimumScaleFactor(0.2)
                            Text("🙏")
                                .font(.system(size: 13))
                                .padding(.leading, 4)
                                .offset(y: -halfDpCeil)
                        }
                    }
                )
                .frame(height: HomeScreen__itemCircleHeight + 2)
                .padding(.horizontal, 8)
                .background(roundedShape.fill(.white))

                Button(
                    action: {
                        homeVm.onRateCancel()
                    },
                    label: {
                        Text(homeState.rateNoThanks)
                            .font(
                                .system(
                                    size: HomeScreen__itemCircleFontSize,
                                    weight: .medium
                                )
                            )
                            .foregroundColor(.white)
                    }
                )
                .padding(.leading, 12)

                Spacer()
            }
            .padding(.top, 9)
            .padding(.leading, 12 - onePx)
        }
        .fillMaxWidth()
        .padding(.top, 12)
        .padding(.bottom, 12)
        .background(shape.fill(.blue))
        .padding(.horizontal, HomeScreen__hPadding)
        .padding(.bottom, bottomMargin)
        .padding(.top, 4)
    }
}
