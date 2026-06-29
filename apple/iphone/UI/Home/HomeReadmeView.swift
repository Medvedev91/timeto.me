import SwiftUI

private let bottomMargin: CGFloat = (HomeScreen__itemHeight - HomeScreen__itemCircleHeight) / 2
private let shape = RoundedRectangle(cornerRadius: 14, style: .continuous)

// todo remove after update July 2026
struct HomeReadmeView: View {
    
    let title: String
    let buttonText: String
    
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        VStack {
            
            Text(title)
                .textAlign(.center)
                .fontWeight(.medium)
            
            Button(
                action: {
                    navigation.fullScreen {
                        DocFullScreen(
                            forceRead: true
                        )
                    }
                },
                label: {
                    Text(buttonText)
                        .font(
                            .system(
                                size: HomeScreen__itemCircleFontSize,
                                weight: HomeScreen__itemCircleFontWeight
                            )
                        )
                        .foregroundColor(.black.opacity(0.8))
                        .frame(height: HomeScreen__itemCircleHeight + 2)
                        .padding(.horizontal, 10)
                        .background(roundedShape.fill(.white))
                        .padding(.top, 10)
                }
            )
        }
        .fillMaxWidth()
        .padding(.top, 12)
        .padding(.bottom, 14)
        .background(shape.fill(.blue))
        .padding(.horizontal, HomeScreen__hPadding)
        .padding(.bottom, bottomMargin)
    }
}
