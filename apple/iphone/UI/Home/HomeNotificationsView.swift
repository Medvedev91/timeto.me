import SwiftUI
import shared

struct HomeNotificationsView: View {
    
    let notificationsPermissionUi: HomeVm.NotificationsPermissionUi

    var body: some View {
        HStack {
            
            Image(systemName: "bell.slash")
                .padding(.leading, 4)
                .foregroundColor(.white)
                .font(.system(size: 44, weight: .regular))
            
            VStack(alignment: .leading) {
                
                Text(notificationsPermissionUi.title)
                    .textAlign(.leading)
                    .fontWeight(.medium)
                
                Button(
                    action: {
                        openSystemSettings()
                    },
                    label: {
                        Text(notificationsPermissionUi.buttonText)
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
            .padding(.leading, 12)
        }
        .fillMaxWidth()
        .padding(.top, 12)
        .padding(.bottom, 20)
        .padding(.horizontal, HomeScreen__hPadding)
    }
}
