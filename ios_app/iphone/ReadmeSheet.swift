import SwiftUI
import shared

private let hPadding = MyListView.PADDING_OUTER_HORIZONTAL
private let pTextLineHeight = 4.0

struct ReadmeSheet: View {

    @Binding var isPresented: Bool

    @State private var vm = ReadmeSheetVM()
    @State private var scroll = 0

    var body: some View {

        VMView(vm: vm, stack: .VStack()) { state in

            Sheet__HeaderView(
                title: state.title,
                scrollToHeader: scroll,
                bgColor: c.sheetBg
            )

            ScrollViewWithVListener(showsIndicators: false, vScroll: $scroll) {

                VStack {

                    ForEachIndexed(state.paragraphs) { _, paragraph in

                        if let paragraph = paragraph as? ReadmeSheetVM.ParagraphTitle {
                            HStack {
                                Text(paragraph.text)
                                    .font(.system(size: 28, weight: .bold))
                                    .padding(.top, 44)
                                    .padding(.horizontal, hPadding)
                                Spacer()
                            }
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphSubtitle {
                            HStack {
                                Text(paragraph.text)
                                    .font(.system(size: 20, weight: .bold))
                                    .padding(.top, 36)
                                    .padding(.horizontal, hPadding)
                                Spacer()
                            }
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphText {
                            HStack {
                                Text(paragraph.text)
                                    .font(.system(size: 18))
                                    .padding(.top, 24)
                                    .padding(.horizontal, hPadding)
                                    .lineSpacing(pTextLineHeight)
                                Spacer()
                            }
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphRedText {
                            HStack {
                                Text(paragraph.text)
                                    .font(.system(size: 18))
                                    .padding(.top, 16)
                                    .padding(.bottom, 16)
                                    .padding(.horizontal, hPadding)
                                    .lineSpacing(pTextLineHeight)
                                Spacer()
                            }
                            .background(c.red)
                            .padding(.top, 24)
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphListDash {
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphTimerTypical {
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphTimerCharts {
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphTimerMyActivities {
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphTimerPractice1 {
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphTimerPractice2 {
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphRepeatingsMy {
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphRepeatingsToday {
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphRepeatingsPractice1 {
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphRepeatingsPractice2 {
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphChecklistsExamples {
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphChecklistsPractice1 {
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphChecklistsPractice2 {
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphGoalsExamples {
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphCalendarExamples {
                        } else if let paragraph = paragraph as? ReadmeSheetVM.ParagraphAskAQuestion {
                            AskAQuestionButtonView(
                                subject: paragraph.subject,
                                isFirst: true,
                                isLast: true,
                                withTopDivider: false
                            )
                                .padding(.top, 36)
                        } else {
                            fatalError()
                        }
                    }

                    Spacer()
                        .padding(.top, 36)
                }
            }

            Sheet__BottomViewClose {
                isPresented = false
            }
        }
    }
}
