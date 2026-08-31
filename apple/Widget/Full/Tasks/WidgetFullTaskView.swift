import SwiftUI
import shared

struct WidgetFullTaskView: View {
    
    let homeTaskUi: HomeTasksItemUi.HomeTaskUi
    
    var body: some View {
        
        HStack {
            
            if let timeUi = homeTaskUi.timeUi {
                let bgColor: Color = switch timeUi.status {
                case .in: widgetFgColor
                case .soon: .blue
                case .overdue: .red
                default: fatalError("timeUi.status bgColor not handled")
                }
                Text(timeUi.text)
                    .foregroundColor(.white)
                    .font(.system(size: widgetFullItemCircleFontSize, weight: widgetFullItemCircleFontWeight))
                    .padding(.horizontal, widgetFullItemCircleHPadding)
                    .frame(height: widgetFullItemCircleHeight)
                    .background(roundedShape.fill(bgColor))
                    .padding(.trailing, homeTaskUi.taskUi.tf.paused != nil ? 9 : 8)
            }
            
            if homeTaskUi.taskUi.tf.paused != nil {
                ZStack {
                    Image(systemName: "pause")
                        .foregroundColor(.white)
                        .font(.system(size: 12, weight: .black))
                }
                .frame(width: widgetFullItemCircleHeight, height: widgetFullItemCircleHeight)
                .background(roundedShape.fill(.green))
                .padding(.trailing, 8)
            }
            
            if let activityUi = homeTaskUi.taskUi.activityUi {
                let symbol: Symbol = activityUi.symbol
                HStack {
                    
                    let offsetX: CGFloat = {
                        if symbol is Symbol.Letter {
                            return 0
                        }
                        if symbol is Symbol.Icon {
                            return -2
                        }
                        if symbol is Symbol.Emoji {
                            return -2
                        }
                        return 0
                    }()
                    
                    SymbolView(
                        symbol: symbol,
                        color: activityUi.colorRgba.toColor(),
                        letterSize: widgetFullFontSize,
                        iconSize: 14,
                        emojiSize: widgetFullFontSize,
                    )
                    .offset(x: offsetX)
                    
                    Spacer()
                }
                .frame(width: 20)
            }
            
            Text(homeTaskUi.text)
                .font(.system(size: widgetFullFontSize))
                .foregroundColor(.white)
                .padding(.trailing, 4)
            
            Spacer()
            
            // todo
            /*
            if let timeUi = homeTaskUi.timeUi {
                let noteColor: Color = switch timeUi.status {
                case .in: .secondary
                case .soon: .blue
                case .overdue: .red
                default: fatalError("timeUi.status noteColor not handled")
                }
                /*
                Text(timeUi.note)
                    .foregroundColor(noteColor)
                    .font(.system(size: HomeScreen__primaryFontSize))
                    .padding(.trailing, HomeScreen__itemCircleHPadding)
                */
            }
            */
        }
        .frame(height: widgetFullItemHeight)
        .padding(.leading, widgetFullHPadding)
    }
}
