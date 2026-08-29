import SwiftUI
import shared

struct WidgetFullChecklistView: View {
    
    let widgetChecklistUi: WidgetChecklistUi
    
    var body: some View {
        
        LazyVStack(spacing: 0) {
            
            ForEach(widgetChecklistUi.itemsUi, id: \.itemDb.id) { itemUi in
                
                Link(destination: WidgetLink.ChecklistItem(itemId: itemUi.itemDb.id.toInt()).buildUrl()) {
                    
                    HStack {
                        
                        ChecklistIconView(
                            iconType: itemUi.itemDb.isChecked ? .checked : .unchecked,
                        )
                        
                        Text(itemUi.text)
                            .padding(.vertical, 4)
                            .foregroundColor(.white)
                            .font(.system(size: widgetFullFontSize))
                            .lineLimit(1)
                            .multilineTextAlignment(.leading)
                        
                        // todo
                        /*
                         TriggersIconsView(
                         checklistsDb: itemUi.textFeatures.checklistsDb,
                         shortcutsDb: itemUi.textFeatures.shortcutsDb,
                         )
                         .padding(.top, 1)
                         .padding(.leading, 8)
                         */
                        
                        Spacer()
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .frame(minHeight: widgetFullItemHeight)
                    .contentShape(Rectangle()) // Tap area full width
                    // Inner padding to contextMenu() correct clipping
                    .padding(.leading, widgetFullHPadding)
                    .customListItem()
                }
            }
        }
        .customList()
        .scrollIndicators(.hidden)
    }
}

private enum ChecklistIconType {
    case checked
    case unchecked
}

private struct ChecklistIconView: View {
    
    let iconType: ChecklistIconType
    
    ///
    
    private var isFilled: Bool {
        iconType == .checked
    }
    
    var body: some View {
        ZStack {
            if iconType == .checked {
                Image(systemName: "checkmark")
                    .foregroundColor(.black)
                    .font(.system(size: 13, weight: .semibold))
            }
        }
        .frame(width: widgetFullItemCircleHeight, height: widgetFullItemCircleHeight)
        .background(
            Circle()
                .fill(isFilled ? .white : .clear)
                .strokeBorder(isFilled ? .clear : widgetFgColor, lineWidth: 2)
                .background(.clear)
        )
        .padding(.trailing, 8)
    }
}
