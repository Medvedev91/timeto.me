import SwiftUI
import shared

struct HomeTasksTomorrowView: View {
    
    let tomorrowUi: HomeTasksItemUi.HomeTomorrowItemUi
    
    var body: some View {
        
        HStack {
            
            TypeIcon(
                type: tomorrowUi.type,
            )
            
            if let timeUi = tomorrowUi.timeUi {
                Text(timeUi.text)
                    .foregroundColor(.white)
                    .font(.system(size: HomeScreen__itemCircleFontSize, weight: HomeScreen__itemCircleFontWeight))
                    .padding(.horizontal, HomeScreen__itemCircleHPadding)
                    .frame(height: HomeScreen__itemCircleHeight)
                    .background(roundedShape.fill(.blue))
                    .padding(.trailing, HomeScreen__itemCircleMarginTrailing)
            }
            
            Text(tomorrowUi.text)
                .font(.system(size: HomeScreen__primaryFontSize))
                .foregroundColor(.white)
                .padding(.trailing, 4)
            
            Spacer()
        }
        .frame(height: HomeScreen__itemHeight)
        .padding(.leading, HomeScreen__hPadding)
    }
}

private struct TypeIcon: View {
    
    let type: HomeTasksItemUi.HomeTomorrowItemUiTomorrowType
    
    var body: some View {
        ZStack {
            Image(systemName: {
                switch type {
                case .repeating: return "repeat"
                case .calendar: return "calendar"
                default: return ""
                }
            }())
            .foregroundColor(.blue)
            .font(.system(size: 18))
        }
        .fillMaxHeight()
        .padding(.trailing, {
            switch type {
            case .repeating: return 9
            case .calendar: return 8
            default: return 0
            }
        }())
    }
}
